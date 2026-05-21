# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Working Directory

**IMPORTANT**: Always work directly in `C:\Users\joels\Projects\Materias\Plataformas Moviles\VialReport\` (the local project), NOT in `.claude/worktrees/`. The user has Android Studio open and needs to see changes in real-time.

---

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
./gradlew build
./gradlew assembleDebug
./gradlew installDebug
./gradlew test
./gradlew test --tests "com.vialreport.app.ExampleUnitTest"
```

### Backend (Ktor)

Run from `Backend/`:

```bash
./gradlew build
./gradlew :app:run
./gradlew :app:shadowJar
./gradlew test
```

Backend reads all config from environment variables: `MONGODB_URI`, `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, `GEMINI_API_KEY` (optional), `UPLOAD_DIR` (default `./uploads`), `PORT` (default 8080).

**Backend deployed at**: `https://proyecto-vialreport-8it4.onrender.com/`

---

## Frontend Architecture

Clean Architecture with MVVM under `app/src/main/java/com/vialreport/app/`:

- **`core/`** — Hilt app entry point, DI modules (`NetworkModule`, `RepositoryModule`), `AuthInterceptor`
- **`data/local/`** — `TokenStore` (SharedPreferences wrapper for JWT)
- **`data/remote/api/`** — `ReportApi`, `AuthApi`
- **`data/remote/dto/`** — `ApiResponseDto<T>`, `ReportDto`, `AuthDto`, `ReportRequestDto`, `UpdateStatusRequestDto`
- **`data/remote/mapper/`** — `ReportMapper` (DTO → domain)
- **`data/repository/`** — `ReportRepositoryImpl`, `AuthRepositoryImpl`
- **`domain/model/`** — `Report`, `IncidentType`, `User`
- **`domain/repository/`** — `IReportRepository`, `IAuthRepository`
- **`domain/usecase/auth/`** — `LoginUseCase` (returns `Result<User>`), `RegisterUseCase` (returns `Result<User>`), `IsLoggedInUseCase`, `LogoutUseCase`
- **`domain/usecase/report/`** — `GetAllReportsUseCase`, `GetReportByIdUseCase`, `CreateReportUseCase`, `UpdateReportUseCase`, `DeleteReportUseCase`, `GetIncidentTypesUseCase`
- **`presentation/auth/login/`** — `LoginScreen`, `LoginViewModel`, `LoginUiState` (sealed: Idle/Loading/Success/Error)
- **`presentation/auth/register/`** — `RegisterScreen`, `RegisterViewModel`, `RegisterUiState` (sealed)
- **`presentation/report/`** — list, detail, form screens with ViewModels and UiStates
- **`presentation/navigation/`** — `AppNavGraph`, `Routes`

**Auth flow**: `AppNavGraph` checks `TokenStore.token` on start → LOGIN if null, LIST if present. After login/register the token is saved automatically. `AuthInterceptor` injects `Authorization: Bearer <token>` in every OkHttp request.

**API base URL**: `https://proyecto-vialreport-8it4.onrender.com/`

**State management**: `StateFlow` + `viewModelScope`. `LoginUiState`/`RegisterUiState` use sealed classes. Report ViewModels use data class UiState.

**Navigation routes** (`Routes.kt`): `LOGIN`, `REGISTER`, `LIST`, `DETAIL/{id}`, `FORM?id={id}`.

**Incident types**: Loaded from public `/incident-types` endpoint (no auth needed) on form open. The form dropdown shows real backend types with emoji icons.

**DI**: Hilt. `RepositoryModule` binds both `IReportRepository` and `IAuthRepository`.

---

## Backend Architecture

Service-oriented with Repository pattern under `app/src/main/java/com/vialreport/backend/`:

- **`config/`** — `Database.kt` (MongoDB Atlas via `MongoClient`; seeds 8 default `IncidentType` docs on first run), `Security.kt` (JWT config)
- **`model/`** — Kotlin data classes backed by BSON `ObjectId`: `Report`, `User`, `Crew`, `IncidentType`, `Notification`, `ReportPhoto`, `ReportStatusLog`
- **`dto/`** — Request/response payloads; `ApiResponse<T>` is the generic wrapper
- **`repository/`** — MongoDB coroutine driver operations
- **`service/`** — Business logic; `PhotoAiService` calls Gemini API to validate uploaded images
- **`routes/`** — `ReportRoutes`, `AuthRoutes`, `CrewRoutes`, `NotificationRoutes`, `IncidentTypeRoutes`, `MapRoutes`, `AdminRoutes`
- **`util/`** — Exceptions, `UserRole` (citizen/admin/crew_member), `ReportStatus`, `ReportPriority`

**Auth**: JWT (Auth0). All `/reports` routes require auth. `/incident-types` and `/auth/*` are public.

**Key REST endpoints**:
```
POST   /auth/register
POST   /auth/login
GET    /incident-types              — public
GET    /reports                     — auth required (citizen sees own, admin sees all)
GET    /reports/{id}                — auth required
POST   /reports                     — citizen only; body: {typeId, title, description, latitude, longitude, address}
PUT    /reports/{id}/status         — admin only; body: {status, note?}
DELETE /reports/{id}                — auth required
POST   /reports/{id}/photos         — multipart; AI-validated via Gemini
GET    /reports/{id}/photos         — admin only
```

**Database**: MongoDB Atlas. All IDs are MongoDB `ObjectId` strings.

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
