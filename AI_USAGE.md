# AI Usage

This file records how AI tools were used while building Awesome Notes. It is updated as the work goes on, not written at the end.

## Tools and models

| Tool | Model | Used for |
|------|-------|----------|
| Claude Code (desktop app) | Claude Opus 5.5 | Planning, finding conflicts between plans, repository scaffolding, documentation, and explaining and proposing code that I implement |
| Gemini in Android Studio | Gemini (built into the IDE) | In-editor help with Android, Compose and Gradle questions, and code completion |

## How the work is split

- **I decide:** the architecture, the library choices and the scope. The AI proposes and points out trade-offs, and the main choices are summarised in the [README](README.md#architecture).
- **Commits are small** and update this file whenever the AI contributed in a meaningful way.

## Representative sessions

### 1. Reconciling two project plans (Claude Code, Opus 5.5)

- **Prompt (summary):** I pasted two planning notes for the project, written at different times, and asked it to start the repository.
- **What the AI did:** it found where the plans contradicted each other before writing any file: DI (Hilt or a manual container), notes persistence (Room as the source of truth or API only), and minSdk (33 or 26). It asked me to decide instead of picking one in silence. Then it created `README.md`, `.gitignore` and this file.
- **Outcome:** I chose Room as the source of truth and minSdk 33, as the AI recommended.

### 2. Designing UI events without one-off streams (Claude Code, Opus 5.5)

- **Prompt (summary):** I didn't want one-off events through a `Channel`. I proposed keeping a list of actions in the UI state and clearing each one with `onConsumeAction`, and asked what the AI thought of it.
- **What the AI did:** it agreed and linked the idea to Google's "UI events" guidance. It pointed out what to get right:
  - With a sealed `UiState`, the list belongs in a wrapper state, not in every variant.
  - Every action needs an id, so identical messages are not confused.
  - Actions are handled one at a time in a `LaunchedEffect` keyed by that id, and consumed afterwards.
  - Actions don't go into `SavedStateHandle`.
- **Outcome:** I accepted the design. I chose to put the action-consuming logic in a shared helper in `core:ui`, so no screen can forget to consume.

### 3. Fitting the server into the Gradle build (Claude Code, Opus 5.5)

- **Prompt (summary):** Set up the Ktor server skeleton (`GET /health`, `Dockerfile`, `docker-compose.yml`). My plan had the server as a `:server` module of the root Gradle build.
- **What the AI did:** before writing any file, it pointed out that the plan would break the Docker build. Gradle configures every module before running a task, and `:app` applies the Android plugin, which needs the Android SDK. The image has no SDK, so even `:server:buildFatJar` would fail. It offered two options:
  - make `server/` a separate build, reusing the root version catalog and pulled in with `includeBuild("server")`
  - keep `:server` and skip the Android modules when an environment variable is set
- **Outcome:** I chose the separate build. Docker runs `./gradlew -p server buildFatJar` and never touches the Android modules. There is still one version catalog and one Gradle wrapper. I checked it end to end: the tests pass, `docker compose up --build` starts the server, and `GET /health` returns `200 OK`.
## Corrections and rejections

| Date | Tool | What the AI suggested | What I did instead | Why |
|------|------|-----------------------|--------------------|-----|
| 2026-11-17 | Claude Code | Hilt for DI, the standard choice for Android | **Koin** | Personal choice. Koin has no annotation processing, its modules are plain Kotlin and easy to walk through in the review, and it injects `SavedStateHandle` into ViewModels. |
| 2026-11-17 | Claude Code | The README it drafted described one-off events (navigation, snackbars) going through a `Channel`, taken from my first plan | Actions kept in the UI state, each with an id and consumed through `onConsumeAction`; navigation through callbacks | Events in a `Channel` can be lost while nothing collects them, and state is easier to test. This follows Google's UI events guidance. |
