# Repository Guidelines

## Project Structure & Module Organization
This repository is a monorepo with three active applications plus shared docs.

- `way-to-fit-frontend/`: React 19 + TypeScript + Vite admin web. Main code lives in `src/` (`components/`, `features/`, `pages/`, `lib/`, `store/`).
- `way-to-fit-backend/`: Spring Boot 3 + Kotlin API. Production code is under `src/main/kotlin/com/waytofit`, tests under `src/test/kotlin`.
- `way-to-fit-app/`: Flutter participant app. App code lives in `lib/` with `core/` and `features/competition/{data,domain,presentation}`.
- `docs/`: product specs and implementation plans. Check these before large competition-domain changes.

Work from the relevant subproject directory rather than the repo root.

## Build, Test, and Development Commands
- Frontend: `cd way-to-fit-frontend && pnpm dev` starts Vite, `pnpm build` runs TypeScript build plus production bundle, `pnpm lint` runs ESLint.
- Backend: `cd way-to-fit-backend && ./gradlew bootRun` starts the API, `./gradlew build` compiles and tests, `./gradlew test` runs tests only.
- Flutter app: `cd way-to-fit-app && flutter analyze` runs static analysis, `flutter test` runs widget/unit tests, `dart run build_runner build --force-jit` regenerates Retrofit/JSON code.

## Coding Style & Naming Conventions
- Frontend: follow existing TypeScript/React patterns, 2-space indentation, PascalCase for components, camelCase for hooks/utilities, and keep route-aware logic close to `src/features/**`.
- Backend: Kotlin conventions, 4-space indentation, PascalCase types, camelCase members, package paths under `com.waytofit.<domain>`.
- Flutter: keep domain models pure, DTO parsing in `data/dto`, and use `snake_case` filenames with `PascalCase` widgets/classes.
- Use the configured linters: `eslint` for frontend and `flutter_lints` for Flutter.

## Testing Guidelines
- Backend tests belong in `way-to-fit-backend/src/test/kotlin` and should use `*Test.kt`.
- Flutter tests belong in `way-to-fit-app/test` and should use `*_test.dart`.
- The frontend currently has no dedicated test runner configured, so at minimum run `pnpm build` and `pnpm lint` after UI changes.

## Commit & Pull Request Guidelines
Recent history mixes plain imperative messages with prefixes like `docs:` and `chore:`. Prefer concise conventional commits such as `feat: add competition leaderboard filter`.

PRs should state the affected module, summarize behavior changes, link the relevant ticket/spec, and include screenshots for frontend or Flutter UI changes. Keep changes scoped to one module or one logical feature when possible.
