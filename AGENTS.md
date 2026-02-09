# Agent Instructions

## Project

Muck is a web UI for [Bob CI/CD](https://github.com/bob-cd/bob) built with Java 21, Helidon Nima, FreeMarker, HTMX, and DaisyUI/Tailwind CSS.

## Build & Test

- Build: `mvn clean package`
- Compile check: `mvn compile`
- Run tests: `mvn test`
- Run app: `java -jar target/muck-1.0.0.jar`

## Workflow

- Run `mvn compile` after making changes to verify they compile
- Run `mvn test` after making changes to verify tests pass
- Add tests when adding features or fixing bugs — only test actual logic, not framework plumbing

## Code Style

- Follow [JAVA_STYLE.md](JAVA_STYLE.md) — use `var`, records, switch expressions, text blocks
- Follow [.editorconfig](.editorconfig) — 4 spaces for Java/FTL/XML, 2 spaces for YAML/Markdown, LF line endings
- Max line length: 120 characters for Java
- Use `java.util.logging` for logging

## Architecture

- `src/main/java/muck/Main.java` — entry point, all route registration
- `src/main/java/muck/client/BobClient.java` — HTTP client for Bob API, discovers endpoints from OpenAPI spec
- `src/main/java/muck/handlers/` — one handler class per route, implements Helidon `Handler`
- `src/main/java/muck/model/` — records for Pipeline, Run
- `src/main/resources/templates/` — FreeMarker templates using `layout.ftl` macro for shared sidebar/nav
- `src/main/resources/static/` — CSS, JS, images

## Conventions

- Handlers take `BobClient` (and optionally FreeMarker `Configuration`) via constructor injection
- Page handlers render FreeMarker templates; action handlers return status codes or HX-Redirect headers
- Templates use HTMX (`hx-get`, `hx-post`, `hx-delete`, `hx-swap`) for dynamic updates
- All pages pass `connected` and `bobUrl` to the layout macro for the sidebar status badge
- DaisyUI components: `table table-lg` for data, `btn` for actions, `badge` for status, `modal` for forms
