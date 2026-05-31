import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../auth/auth_session.dart';
import '../../../features/auth/presentation/screens/login_screen.dart';
import '../../../features/competition/domain/models.dart';
import '../../../features/competition/presentation/screens/athlete_profile_screen.dart';
import '../../../features/competition/presentation/screens/competition_detail_screen.dart';
import '../../../features/competition/presentation/screens/competition_list_screen.dart';
import '../../../features/competition/presentation/screens/event_lineup_screen.dart';
import '../../../features/competition/presentation/screens/individual_reg_screen.dart';
import '../../../features/competition/presentation/screens/leaderboard_screen.dart';
import '../../../features/competition/presentation/screens/my_profile_screen.dart';
import '../../../features/competition/presentation/screens/score_submit_screen.dart';
import '../../../features/competition/presentation/screens/team_reg_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final notifier = ValueNotifier<int>(0);
  ref.listen(authControllerProvider, (prev, next) => notifier.value++);
  ref.onDispose(notifier.dispose);

  return GoRouter(
    initialLocation: '/competitions',
    refreshListenable: notifier,
    redirect: (context, state) {
      final authValue = ref.read(authControllerProvider);
      if (authValue.isLoading) return null;

      final authState = authValue.valueOrNull;
      final isAuthenticated = authState?.isAuthenticated ?? false;
      final isLoginRoute = state.matchedLocation == '/login';
      final isMyProfileRoute = state.matchedLocation == '/me';

      if (!isAuthenticated && isMyProfileRoute) {
        final from = Uri.encodeComponent(state.uri.toString());
        return '/login?from=$from';
      }
      if (isAuthenticated && isLoginRoute) {
        final from = state.uri.queryParameters['from'];
        if (from != null && from.isNotEmpty) {
          return Uri.decodeComponent(from);
        }
        return '/competitions';
      }
      return null;
    },
    routes: [
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) =>
            _AppShell(navigationShell: navigationShell),
        branches: [
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/competitions',
                builder: (context, state) => const CompetitionListScreen(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/me',
                builder: (context, state) => const MyProfileScreen(),
              ),
            ],
          ),
        ],
      ),
      GoRoute(
        path: '/competitions/:competitionId',
        builder: (_, state) => CompetitionDetailScreen(
          competitionId: state.pathParameters['competitionId']!,
        ),
        routes: [
          GoRoute(
            path: 'leaderboard',
            builder: (_, state) => LeaderboardScreen(
              competitionId: state.pathParameters['competitionId']!,
            ),
          ),
          GoRoute(
            path: 'register/individual',
            builder: (_, state) {
              final scaleCategories =
                  (state.extra as List<String>?) ?? const <String>[];
              return IndividualRegScreen(
                competitionId: state.pathParameters['competitionId']!,
                scaleCategories: scaleCategories,
              );
            },
          ),
          GoRoute(
            path: 'register/team',
            builder: (_, state) {
              final scaleCategories =
                  (state.extra as List<String>?) ?? const <String>[];
              return TeamRegScreen(
                competitionId: state.pathParameters['competitionId']!,
                scaleCategories: scaleCategories,
              );
            },
          ),
          GoRoute(
            path: 'submit-score',
            builder: (_, state) => ScoreSubmitScreen(
              competitionId: state.pathParameters['competitionId']!,
              eventId: state.uri.queryParameters['eventId'] ?? '',
              registrationId: state.uri.queryParameters['registrationId'] ?? '',
            ),
          ),
          GoRoute(
            path: 'lineup',
            builder: (_, state) {
              final members = (state.extra as List<TeamMember>?) ?? const [];
              return EventLineupScreen(
                competitionId: state.pathParameters['competitionId']!,
                registrationId:
                    state.uri.queryParameters['registrationId'] ?? '',
                members: members,
              );
            },
          ),
        ],
      ),
      GoRoute(
        path: '/athletes/:userId',
        builder: (_, state) =>
            AthleteProfileScreen(userId: state.pathParameters['userId']!),
      ),
      GoRoute(path: '/login', builder: (context, state) => const LoginScreen()),
    ],
    errorBuilder: (_, state) =>
        Scaffold(body: Center(child: Text('페이지를 찾을 수 없습니다: ${state.uri}'))),
  );
});

class _AppShell extends StatelessWidget {
  const _AppShell({required this.navigationShell});

  final StatefulNavigationShell navigationShell;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: navigationShell,
      bottomNavigationBar: NavigationBar(
        selectedIndex: navigationShell.currentIndex,
        onDestinationSelected: (index) {
          navigationShell.goBranch(
            index,
            initialLocation: index == navigationShell.currentIndex,
          );
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.emoji_events_outlined),
            selectedIcon: Icon(Icons.emoji_events),
            label: '대회',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person),
            label: '내 프로필',
          ),
        ],
      ),
    );
  }
}
