# Repository Guidelines

## Project Structure & Module Organization
FixFlow is a ticket-management application using Java 21, Spring Boot, MyBatis-Plus, MySQL, and Vue 3 with TypeScript.
- `src/main/java/com/lz/fixflow/`: backend controllers, services, service implementations, mappers, entities, DTOs, VOs, authentication, and auditing. Keep business rules in services.
- `src/main/resources/`: application profiles and Flyway migrations under `db/migration/`.
- `src/test/java/com/lz/fixflow/`: backend unit and integration tests.
- `frontend/src/`: views, layouts, reusable components in `component/`, API clients, Pinia stores, router, utilities, and assets. Static files live in `frontend/public/`.
- `docker/`, root `Dockerfile`, and `docker-compose.yml`: deployment configuration. Generated outputs are `target/` and `frontend/dist/`.

## Build, Test, and Development Commands
Use PowerShell 7 on Windows. Run backend commands from the repository root:
- `.\mvnw.cmd spring-boot:run`: start the backend; configure local MySQL first.
- `.\mvnw.cmd test`: run backend tests; database-backed tests require MySQL.
- `.\mvnw.cmd '-Dtest=TicketServiceTest' test`: run focused Mockito service tests.
- `.\mvnw.cmd package`: test and package the backend.

From `frontend/`:
- `npm ci`: install locked dependencies.
- `npm run dev`: start Vite on port 3000, proxying `/api` to port 8080.
- `npm run build`: run Vue/TypeScript checks and create the production bundle.
- `npm run preview`: preview the built frontend.

For the full stack, configure `.env` from `.env.example`, then run `docker compose up -d --build`.

## Coding Style & Naming Conventions
Match surrounding formatting: Java generally uses four spaces; Vue templates use two. Preserve existing TypeScript formatting. Use PascalCase classes/components, camelCase methods/variables, and descriptive `DTO`, `VO`, `Service`, and `Mapper` suffixes. Name new migrations `V<number>__<description>.sql`; do not rewrite applied migrations. No dedicated formatter or linter is configured.

## Testing Guidelines
Tests use JUnit Jupiter, Mockito, and Spring Boot testing. Follow existing `*Test`, `*Tests`, and `*IntegrationTest` names. Add regression coverage for changed permissions, ticket transitions, transactions, or auditing. Integration tests using the `test` profile need the separate `fixflow_test` database; avoid parallel execution when tests replace global Sa-Token state. No coverage threshold or frontend test runner is configured; build and manually check affected UI flows.

## Commit & Pull Request Guidelines
Recent commits use `type(scope): description`, such as `fix(ticket): ...`; Chinese descriptions are common. Keep changes focused. PRs should explain behavior changes, link relevant issues, report verification, and include screenshots for UI changes and migration/configuration notes when applicable.

## Security & Configuration
Never commit `.env` or credentials. Enforce permissions server-side. Avoid `docker compose down -v` unless intentionally deleting database volumes.