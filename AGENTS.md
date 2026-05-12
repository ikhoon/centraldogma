# Repository Guidelines

## Project Structure & Module Organization
Central Dogma is a multi-module Gradle build. Core Java code lives under `common/`, `server/`, `client/`, and supporting modules like `server-auth/`, `server-mirror-git/`, `testing/`, and `testing-internal/`. Integration tests live in `it/`. The admin UI is a Next.js app in `webapp/`. Gradle configuration is in `build.gradle`, `settings.gradle`, and `gradle/` scripts; IDE and lint configs are in `settings/`. Docs and site content are under `docs/` and `site/`.

## Build, Test, and Development Commands
- `./gradlew build`: compiles all modules and builds artifacts.
- `./gradlew test`: runs unit tests across modules.
- `./gradlew check`: runs tests plus lint/checkstyle tasks.
- `./gradlew build site`: builds the project and the documentation site (recommended before PRs).
- `./gradlew :<module>:test`: run tests for a specific module, e.g. `./gradlew :server:test`.
- `npm install` in `webapp/`: install UI dependencies.
- `npm run develop` in `webapp/`: run the admin UI against a local backend; `npm run backend` starts a test backend.

Ensure JDK 13+ is available for Gradle builds.

## Coding Style & Naming Conventions
Use the LINE OSS code style and inspection profiles from `settings/` (IntelliJ/Eclipse configs are provided). Java style is enforced by Checkstyle (`settings/checkstyle/checkstyle.xml`) and Kotlin modules use Ktlint. Keep class names in `UpperCamelCase`, methods/fields in `lowerCamelCase`, and avoid unnecessary `public` visibility where not needed. For the UI, follow ESLint and Prettier via `npm run lint` and `npm run format`.

## Testing Guidelines
Java tests use JUnit 5 with Mockito/AssertJ; tests live under `*/src/test/java`, with integration tests in `it/` (often named `*IntegrationTest`). Run module-specific tests with `./gradlew :path:test`. The webapp uses Jest (`npm run test` or `npm run test:ci`) and Playwright for E2E (`npm run test:e2e`).

## Commit & Pull Request Guidelines
Recent commit subjects are short, imperative, and often include a PR number in parentheses, e.g. “Add … (#1234)”. Keep commits focused. For PRs, link issues where applicable, ensure `./gradlew build site` passes, address Checkstyle/Javadoc warnings, and sign the CLA for non-trivial changes.
