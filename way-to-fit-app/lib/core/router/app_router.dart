import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/screens/login_screen.dart';
import '../../features/competition/presentation/screens/athlete_profile_screen.dart';
import '../../features/competition/presentation/screens/competition_detail_screen.dart';
import '../../features/competition/presentation/screens/competition_list_screen.dart';
import '../../features/competition/presentation/screens/individual_reg_screen.dart';
import '../../features/competition/presentation/screens/leaderboard_screen.dart';
import '../../features/competition/presentation/screens/score_submit_screen.dart';
import '../../features/competition/presentation/screens/team_reg_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/competitions',
    routes: [
      GoRoute(
        path: '/competitions',
        builder: (context, state) => const CompetitionListScreen(),
        routes: [
          GoRoute(
            path: ':competitionId',
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
                builder: (_, state) => IndividualRegScreen(
                  competitionId: state.pathParameters['competitionId']!,
                ),
              ),
              GoRoute(
                path: 'register/team',
                builder: (_, state) => TeamRegScreen(
                  competitionId: state.pathParameters['competitionId']!,
                ),
              ),
              GoRoute(
                path: 'submit-score',
                builder: (_, state) => ScoreSubmitScreen(
                  competitionId: state.pathParameters['competitionId']!,
                  eventId: state.uri.queryParameters['eventId'] ?? '',
                  registrationId:
                      state.uri.queryParameters['registrationId'] ?? '',
                ),
              ),
            ],
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
