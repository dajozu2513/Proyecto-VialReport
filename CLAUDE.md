# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VialReport is a road incident reporting application with two separate Gradle projects:
- **Frontend/** — Android app (Kotlin + Jetpack Compose)
- **Backend/** — Ktor REST server (Kotlin)

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
```

### Backend (Ktor)

Run from `Backend/`:

```bash
./gradlew build                    # Full build
./gradlew :app:run                 # Run the server
./gradlew :app:shadowJar           # Build fat JAR
./gradlew test                     # Unit tests
```

Backend requires database credentials in `Backend/local.properties`.

---

## Frontend Architecture

Clean Architecture with MVVM, organized into four layers:

- **`core/`** — Hilt application entry point (`VialReportApp.kt`) and DI modules (`NetworkModule`, `RepositoryModule`)
- **`data/`** — Retrofit API interface (`ReportApi`), DTOs, mappers, and `ReportRepositoryImpl`
- **`domain/`** — Domain models, `IReportRepository` interface, and use cases (one class per operation)
- **`presentation/`** — Compose screens, ViewModels, and UiState per feature; navigation graph in `AppNavGraph.kt`

**State management**: `StateFlow` + `viewModelScope` coroutines. Each screen has a dedicated ViewModel with a sealed `UiState`.

**Navigation**: Jetpack Compose Navigation with three routes defined in `Routes.kt`: LIST, DETAIL, FORM. Detail and Form receive an optional report ID as a query parameter.

**API**: Currently points to a MockAPI endpoint (`https://69d49ba8d396bd74235d42e9.mockapi.io/api/v1/`). Configured in `NetworkModule` with GSON and OkHttp logging at BODY level.

**DI**: Hilt throughout. Modules are in `core/di/`.

---

## Backend Architecture

Service-oriented with Repository pattern:

- **`config/`** — `Database.kt` (HikariCP + Exposed setup), `Security.kt` (JWT config)
- **`model/`** — Exposed table definitions and data classes: `Report`, `User`, `Crew`, `IncidentType`, `Notification`, `ReportPhoto`, `ReportStatusLog`
- **`dto/`** — Request/response payloads decoupled from DB models
- **`repository/`** — Exposed DAO operations (suspend functions wrapped in `newSuspendedTransaction`)
- **`service/`** — Business logic; services call repositories and enforce rules
- **`routes/`** — Ktor routing; each file registers routes for a resource
- **`util/`** — Custom exception classes and role constants (`ADMIN`, `CITIZEN`)

**Auth**: JWT via Auth0 library. Passwords hashed with jBCrypt. Routes are protected by role: citizens can only see their own reports; admins see all and can update status.

**Key REST endpoints** (in `ReportRoutes.kt`):
```
GET    /reports          — list (role-filtered)
GET    /reports/{id}     — detail
POST   /reports          — create
PUT    /reports/{id}/status — update status (admin only)
DELETE /reports/{id}     — delete
```

**Database**: MySQL via HikariCP connection pool. Schema is created via `SchemaUtils.create()` on startup.

---

## Key Technology Versions

| Area | Tech | Version |
|------|------|---------|
| Frontend language | Kotlin | 2.0.21 |
| UI | Jetpack Compose BOM | 2025.08.00 |
| DI | Hilt | 2.57.1 |
| HTTP | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Navigation | Jetpack Navigation | 2.9.3 |
| Backend framework | Ktor | 2.3.7 |
| ORM | Exposed | 0.45.0 |
| DB driver | MySQL Connector | 8.0.33 |
| Android min SDK | — | 24 |
| Android target SDK | — | 36 |
