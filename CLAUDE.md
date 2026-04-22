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

Backend requires database credentials in `Backend/local.properties` (not committed).

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

- **`config/`** — `Database.kt` (HikariCP pool: 10 max / 2 min idle, Exposed setup, schema auto-creation, seed data for 8 default `IncidentType` entries), `Security.kt` (JWT config)
- **`model/`** — Exposed `IntIdTable` definitions and data classes: `Report`, `User`, `Crew`, `IncidentType`, `Notification`, `ReportPhoto`, `ReportStatusLog`
- **`dto/`** — Request/response payloads decoupled from DB models; `ApiResponse<T>` is the generic response wrapper
- **`repository/`** — Exposed DAO operations; all DB access uses `newSuspendedTransaction`
- **`service/`** — Business logic; services call repositories and enforce rules
- **`routes/`** — Ktor routing; each file registers routes for a resource
- **`util/`** — Custom exceptions (`NotFoundException`, `UnauthorizedException`, `BadRequestException`, `ConflictException`), role constants (`ADMIN`, `CITIZEN`, `CREW_MEMBER`), and `ReportStatus`/`ReportPriority` validators

**Application startup** (`Application.kt`): database init → content negotiation (kotlinx.serialization) → CORS (any host) → global error handling via `StatusPages` → JWT auth → manual DI (repositories and services instantiated and passed to routes).

**Auth**: JWT via Auth0 library. Passwords hashed with jBCrypt. Claims carry `role` and `userId`. Citizens see only their own reports; admins see all and can change status.

**Key REST endpoints** (in `ReportRoutes.kt`):
```
GET    /reports             — list (role-filtered)
GET    /reports/{id}        — detail
POST   /reports             — create (citizen only)
PUT    /reports/{id}/status — update status (admin only)
DELETE /reports/{id}        — delete
```

Additional route files: `AuthRoutes.kt`, `CrewRoutes.kt`, `NotificationRoutes.kt`.

**Database**: MySQL via HikariCP. Schema is created via `SchemaUtils.create()` on startup (non-destructive).

---

## Key Technology Versions

| Area | Tech | Version |
|------|------|---------|
| Frontend language | Kotlin | 2.0.21 |
| UI | Jetpack Compose BOM | 2025.08.00 |
| DI | Hilt | 2.57.1 |
| HTTP | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Navigation | Jetpack Navigation | 2.9.3 |
| Backend language | Kotlin | 1.9.22 |
| Backend framework | Ktor | 2.3.7 |
| ORM | Exposed | 0.45.0 |
| DB driver | MySQL Connector | 8.0.33 |
| Connection pool | HikariCP | 5.0.1 |
| Auth | Auth0 JWT | 4.4.0 |
| Android min SDK | — | 24 |
| Android target SDK | — | 36 |
