# Awesome Notes

A notes app for Android with its own backend: register, log in and out, and create, edit and delete notes.

- **Android app:** Kotlin, Jetpack Compose and Coroutines in a multi-module project. Needs Android 13 (API 33) or newer.
- **Backend:** Ktor and PostgreSQL. One command starts both with Docker Compose.
- **Contract:** [`api/openapi.yaml`](api/openapi.yaml) (OpenAPI 3.0.3) is the only thing the app and the server share.

## Repository layout

```
awesome-notes/
├── api/openapi.yaml       # API contract (source of truth for app and server)
├── app/                   # Android app module (composition root)
├── core/                  # domain, data, network, storage, database, ui
├── feature/               # auth, notes
├── server/                # Ktor backend (Gradle module :server, own Dockerfile)
├── docker-compose.yml
└── AI_USAGE.md            # How AI tools were used in this project
```

## Architecture

### Android app

```
feature:auth, feature:notes ──► core:domain ◄── core:data ──► core:network, core:database, core:storage
                └──────────────► core:ui
app ──► everything (Koin composition root, navigation)
```

| Module | Responsibility |
|--------|----------------|
| `core:domain` | Pure Kotlin: `Note`, `AppError`, repository interfaces |
| `core:data` | Repository implementations, mapping between DTOs, entities and domain models |
| `core:network` | Ktor `HttpClient` (OkHttp, Bearer auth), `AuthApi`, `NotesApi` |
| `core:database` | Room database, `NoteEntity`, `NoteDao` |
| `core:storage` | `TokenStore` (DataStore) for the session token |
| `core:ui` | Material 3 theme, shared components (loading, empty, error) and the shared helper that handles and consumes actions |
| `feature:auth`, `feature:notes` | Screens and ViewModels. They depend only on `core:domain` and `core:ui` |

Main choices:

- **Feature modules see only interfaces.** They depend only on `core:domain` and `core:ui`, never on `core:data`, and the compiler enforces it.
- **Room is the source of truth for notes.** The UI observes Room, and the repository syncs it with the API.
- **One `StateFlow<UiState>` per screen.** `UiState` is a sealed `Loading`, `Empty`, `Error` or `Content`.
- **No one-off event streams.** Things the screen must do once, such as showing a snackbar or reacting to a successful save, are kept in the state as a list of actions, each with an id. The screen handles them one at a time and calls `onConsumeAction(id)` to remove each one. A shared helper in `core:ui` does this for every screen. Navigation is done through screen callbacks.
- **Process death:** form state and the editor draft live in `SavedStateHandle`.
- **Libraries:** Koin for dependency injection, Ktor Client with kotlinx.serialization (the same library family as the server), and `minSdk` 33.

### Backend

- Ktor, Exposed and PostgreSQL, started with Docker Compose.
- **Opaque session tokens** stored in a `sessions` table, so logout revokes the session by deleting a row. Passwords are hashed with bcrypt.
- **Every note query is scoped to the authenticated user.** Another user's note returns `404`, not `403`, so the API does not reveal that it exists.
- The app and the server share no code. The only shared artifact is the contract, [`api/openapi.yaml`](api/openapi.yaml).

## Running

> Work in progress. These steps will be filled in as each part lands.

### Backend

```bash
docker compose up --build
```

### Android app

Open the project in Android Studio and run the `app` configuration on an emulator or device with API 33 or newer.

## Documentation

- [AI_USAGE.md](AI_USAGE.md): which AI tools were used, what they were used for, and what was corrected or rejected.
