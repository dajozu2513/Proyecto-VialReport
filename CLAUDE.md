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

To run locally use `Backend/run-backend.ps1` (gitignored — contains credentials). Backend reads all config from environment variables: `MONGODB_URI`, `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, `GEMINI_API_KEY` (optional — skips AI photo filter if blank), `UPLOAD_DIR` (default `./uploads`), `PORT` (default 8080).

**Backend deployed at**: `https://proyecto-vialreport-8it4.onrender.com/`

---

## Frontend Architecture

Clean Architecture with MVVM under `app/src/main/java/com/vialreport/app/`:

- **`core/`** — Hilt app entry point (`VialReportApp`), DI modules (`NetworkModule`, `RepositoryModule`), `AuthInterceptor`
- **`data/local/`** — `TokenStore`: SharedPreferences wrapper storing JWT token, user role, and user name. Exposes `isAdmin: Boolean`
- **`data/remote/api/`** — `ReportApi`, `AuthApi`
- **`data/remote/dto/`** — `ApiResponseDto<T>`, `ReportDto` (includes `PhotoDto` list), `AuthDto`, `ReportRequestDto`, `UpdateStatusRequestDto`, `PhotoDto`
- **`data/remote/mapper/`** — `ReportMapper` (DTO → domain, maps nested photos)
- **`data/repository/`** — `ReportRepositoryImpl`, `AuthRepositoryImpl`
- **`domain/model/`** — `Report` (includes `photos: List<ReportPhoto>`), `ReportPhoto`, `IncidentType`, `User`
- **`domain/repository/`** — `IReportRepository`, `IAuthRepository`
- **`domain/usecase/auth/`** — `LoginUseCase` (returns `Result<User>`), `RegisterUseCase`, `IsLoggedInUseCase`, `LogoutUseCase`
- **`domain/usecase/report/`** — `GetAllReportsUseCase`, `GetReportByIdUseCase`, `CreateReportUseCase`, `UpdateReportUseCase` (citizen edits fields via `PUT /reports/{id}`), `UpdateStatusUseCase` (admin changes status via `PUT /reports/{id}/status`), `DeleteReportUseCase`, `GetIncidentTypesUseCase`, `UploadPhotoUseCase`
- **`presentation/auth/login/`** — `LoginScreen`, `LoginViewModel`, `LoginUiState` (sealed: Idle/Loading/Success/Error)
- **`presentation/auth/register/`** — `RegisterScreen`, `RegisterViewModel`, `RegisterUiState` (sealed)
- **`presentation/report/list/`** — list screen with search + status filter chips, shows logged-in user name in TopAppBar
- **`presentation/report/detail/`** — detail screen with photo gallery (horizontal scroll), photo upload button, admin-only status change section
- **`presentation/report/form/`** — create/edit form with incident type dropdown loaded from backend
- **`presentation/navigation/`** — `AppNavGraph`, `Routes`

**Auth flow**: `AppNavGraph` checks `TokenStore.token` on start → LOGIN if null, LIST if present. After login/register, token + role + userName are saved automatically. `AuthInterceptor` injects `Authorization: Bearer <token>` in every OkHttp request. HTTP 401 login errors show "Correo o contraseña incorrectos".

**API base URL**: `https://proyecto-vialreport-8it4.onrender.com/`

**State management**: `StateFlow` + `viewModelScope`. `LoginUiState`/`RegisterUiState` use sealed classes. Report ViewModels use data class UiState.

**Navigation routes** (`Routes.kt`): `LOGIN`, `REGISTER`, `LIST`, `DETAIL/{id}`, `FORM?id={id}`. List refreshes automatically after form save or admin status change (via `savedStateHandle["refresh_list"]`).

**Incident types**: Loaded from public `/incident-types` endpoint on form open. Dropdown shows emoji + name from backend.

**Photos**: Uploaded via `POST /reports/{id}/photos` (multipart). Gemini AI validates the image is a real road incident before saving. Displayed as horizontal scroll in the detail screen using Coil. Render's file system is ephemeral — photos are lost on redeploy.

**Roles**:
- `citizen` — default on register; sees/edits/deletes own reports, uploads photos
- `admin` — sees all reports, changes status via detail screen; set manually in MongoDB Atlas
- `crew_member` — defined but no specific frontend flow yet

**DI**: Hilt. `RepositoryModule` binds `IReportRepository` and `IAuthRepository`. `TokenStore` is `@Singleton`.

---

## Backend Architecture

Service-oriented with Repository pattern under `app/src/main/java/com/vialreport/backend/`:

- **`config/`** — `Database.kt` (MongoDB Atlas via `MongoClient`; seeds 8 default `IncidentType` docs on first run), `Security.kt` (JWT config)
- **`model/`** — Kotlin data classes backed by BSON `ObjectId`: `Report`, `User`, `Crew`, `IncidentType`, `Notification`, `ReportPhoto`, `ReportStatusLog`
- **`dto/`** — Request/response payloads; `ApiResponse<T>` is the generic wrapper
- **`repository/`** — MongoDB coroutine driver operations (no ORM)
- **`service/`** — Business logic; `PhotoAiService` calls Gemini 1.5 Flash to validate uploaded images; `ReportService` enforces role-based access
- **`routes/`** — `ReportRoutes`, `AuthRoutes`, `CrewRoutes`, `NotificationRoutes`, `IncidentTypeRoutes`, `MapRoutes`, `AdminRoutes`
- **`util/`** — Exceptions, `UserRole` (citizen/admin/crew_member), `ReportStatus`, `ReportPriority`

**Auth**: JWT (Auth0). All `/reports` routes require auth. `/incident-types` and `/auth/*` are public. Passwords hashed with jBCrypt.

**Static files**: `/uploads` served via Ktor `staticFiles` from the `UPLOAD_DIR` folder.

**Key REST endpoints**:
```
POST   /auth/register
POST   /auth/login
GET    /incident-types                — public
GET    /reports                       — citizen sees own; admin sees all (supports ?status=&typeId=&zone=)
GET    /reports/{id}                  — auth required
POST   /reports                       — citizen only; body: {typeId, title, description, latitude, longitude, address}
PUT    /reports/{id}                  — citizen edits own fields; admin edits any
PUT    /reports/{id}/status           — admin only; body: {status, note?}
DELETE /reports/{id}                  — auth required (citizen: own only; admin: any)
POST   /reports/{id}/photos           — multipart; AI-validated via Gemini
GET    /reports/{id}/photos           — admin only
GET    /uploads/{filename}            — static file serving
```

**Database**: MongoDB Atlas. All IDs are MongoDB `ObjectId` strings. To make a user admin: MongoDB Atlas → Browse Collections → `users` collection → change `"role": "citizen"` to `"role": "admin"`, then the user must log out and back in.

---

## Key Technology Versions

| Area | Tech | Version |
|------|------|---------|
| Frontend language | Kotlin | 2.0.21 |
| UI | Jetpack Compose BOM | 2025.08.00 |
| DI | Hilt | 2.57.1 |
| HTTP client (Android) | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Image loading | Coil | 2.7.0 |
| Navigation | Jetpack Navigation | 2.9.3 |
| Backend framework | Ktor | 2.3.7 |
| Database | MongoDB Atlas (Kotlin coroutine driver) | — |
| Auth | Auth0 JWT | 4.4.0 |
| AI photo filter | Google Gemini 1.5 Flash API | — |
| Android min SDK | — | 24 |
| Android target SDK | — | 36 |
