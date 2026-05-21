# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VialReport is a road incident reporting application with two separate Gradle projects:
- **Frontend/** — Android app (Kotlin + Jetpack Compose)
- **Backend/** — Ktor REST server (Kotlin)

Both projects manage dependencies via version catalogs in `gradle/libs.versions.toml`.

---

## Commands

### Frontend (Android)

Run from `Frontend/`:

```bash
./gradlew build                    # Full build
./gradlew assembleDebug            # Debug APK
./gradlew installDebug             # Install on connected device/emulator
./gradlew test                     # Unit tests
./gradlew connectedAndroidTest     # Instrumented tests (requires device)
./gradlew test --tests "com.vialreport.app.ExampleUnitTest"  # Single test class
```

### Backend (Ktor)

Run from `Backend/`:

```bash
./gradlew build                    # Full build
./gradlew :app:run                 # Run the server
./gradlew :app:shadowJar           # Build fat JAR
./gradlew test                     # Unit tests
./gradlew test --tests "com.vialreport.backend.ExampleUnitTest"  # Single test class
```

Backend reads all config from environment variables (see `application.conf`): `MONGODB_URI`, `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, `GEMINI_API_KEY` (optional — skips AI photo filter if blank), `UPLOAD_DIR` (defaults to `./uploads`), `PORT` (defaults to 8080).

---

## Frontend Architecture

Clean Architecture with MVVM, organized into four layers under `app/src/main/java/com/vialreport/app/`:

- **`core/`** — Hilt application entry point (`VialReportApp.kt`) and DI modules (`NetworkModule`, `RepositoryModule`)
- **`data/`** — Retrofit API interface (`ReportApi`), DTOs, mappers, and `ReportRepositoryImpl`
- **`domain/`** — Domain models, `IReportRepository` interface, and use cases (one class per operation in `usecase/report/`)
- **`presentation/`** — Compose screens, ViewModels, and sealed `UiState` per feature; navigation graph in `AppNavGraph.kt`

**State management**: `StateFlow` + `viewModelScope` coroutines. ViewModels combine flows with `combine()` and use `stateIn()`. `SavedStateHandle` persists filter/query state across process death.

**Navigation**: Jetpack Compose Navigation with three routes in `Routes.kt`: LIST, DETAIL (path param `{id}`), FORM (query param `?id={id}`). Screens signal refresh back to the list via `savedStateHandle`.

**API**: Currently points to a MockAPI endpoint (`https://69d49ba8d396bd74235d42e9.mockapi.io/api/v1/`). Base URL is configured in `NetworkModule` with GSON and OkHttp logging at BODY level.

**DI**: Hilt throughout. `RepositoryModule` binds `IReportRepository` → `ReportRepositoryImpl`.

---

## Backend Architecture

Service-oriented with Repository pattern under `app/src/main/java/com/vialreport/backend/`:

- **`config/`** — `Database.kt` (MongoDB Atlas via `MongoClient`; seeds 8 default `IncidentType` docs on first run), `Security.kt` (JWT config)
- **`model/`** — Kotlin data classes backed by MongoDB BSON `ObjectId`: `Report`, `User`, `Crew`, `IncidentType`, `Notification`, `ReportPhoto`, `ReportStatusLog`
- **`dto/`** — Request/response payloads decoupled from DB models; `ApiResponse<T>` is the generic response wrapper
- **`repository/`** — MongoDB coroutine driver operations (no ORM/Exposed)
- **`service/`** — Business logic; services call repositories and enforce rules; `PhotoAiService` calls Gemini API to validate uploaded images
- **`routes/`** — Ktor routing; each file registers routes for a resource
- **`util/`** — Custom exceptions (`NotFoundException`, `UnauthorizedException`, `BadRequestException`, `ConflictException`), role constants (`ADMIN`, `CITIZEN`, `CREW_MEMBER`), and `ReportStatus`/`ReportPriority` validators

**Application startup** (`Application.kt`): MongoDB init → content negotiation (kotlinx.serialization) → CORS (any host) → global error handling via `StatusPages` → JWT auth → manual DI (repositories and services instantiated and passed to routes).

**Auth**: JWT via Auth0 library. Passwords hashed with jBCrypt. Claims carry `role` and `userId`. Citizens see only their own reports; admins see all and can change status.

**Key REST endpoints** (in `ReportRoutes.kt`):
```
GET    /reports               — list (role-filtered; admin supports ?status=&typeId=&zone=)
GET    /reports/{id}          — detail
POST   /reports               — create (citizen only)
PUT    /reports/{id}/status   — update status (admin only)
DELETE /reports/{id}          — delete
POST   /reports/{id}/photos   — upload photo (multipart; AI-validated via Gemini)
GET    /reports/{id}/photos   — list photos (admin only)
```

Additional route files: `AuthRoutes.kt`, `CrewRoutes.kt`, `NotificationRoutes.kt`, `IncidentTypeRoutes.kt`, `MapRoutes.kt`, `AdminRoutes.kt`.

**Database**: MongoDB Atlas. Connection URI and all secrets are supplied via environment variables (see configuration below), not `local.properties`.

---

## Key Technology Versions

| Area | Tech | Version |
|------|------|---------|
| Frontend language | Kotlin | 2.0.21 |
| UI | Jetpack Compose BOM | 2025.08.00 |
| DI | Hilt | 2.57.1 |
| HTTP client (Android) | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Navigation | Jetpack Navigation | 2.9.3 |
| Backend framework | Ktor | 2.3.7 |
| Database | MongoDB Atlas (Kotlin coroutine driver) | — |
| Auth | Auth0 JWT | 4.4.0 |
| AI photo filter | Google Gemini 1.5 Flash API | — |
| Android min SDK | — | 24 |
| Android target SDK | — | 36 |
