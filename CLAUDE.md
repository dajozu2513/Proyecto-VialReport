# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Working Directory

**IMPORTANT**: Always work directly in `C:\Users\joels\Projects\Materias\Plataformas Moviles\VialReport\` (the local project), NOT in `.claude/worktrees/`. The user has Android Studio open and needs to see changes in real-time.

---

## Project Overview

VialReport is a road incident reporting application with two separate Gradle projects:
- **Frontend/** — Android app (Kotlin + Jetpack Compose)
- **Backend/** — Ktor REST server (Kotlin), scaffolded as an Android Studio project but runs as a JVM server via Gradle (`EngineMain` + Netty)

Both projects manage dependencies via version catalogs in `gradle/libs.versions.toml`.

---

## Commands

### Frontend (Android)

Build APK from Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
Output: `Frontend/app/build/outputs/apk/debug/app-debug.apk`

No `gradlew` in Frontend — always build from Android Studio or use Android Studio's terminal.

### Backend (Ktor)

Run from `Backend/`:
```powershell
.\run-backend.ps1   # sets env vars + runs server (gitignored, contains credentials)
```

Or manually:
```powershell
$env:MONGODB_URI  = "mongodb+srv://dajozu2513:Terraria10@cluster0.iakfaa2.mongodb.net/vialreport?appName=Cluster0"
$env:JWT_SECRET   = "cualquier-string-secreto-largo"
$env:JWT_ISSUER   = "vialreport"
$env:JWT_AUDIENCE = "vialreport-users"
.\gradlew :app:run
```

Backend reads all config from environment variables: `MONGODB_URI`, `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, `GEMINI_API_KEY` (optional), `UPLOAD_DIR` (default `./uploads`), `PORT` (default 8080).

**Backend deployed at**: `https://proyecto-vialreport-8it4.onrender.com/`
**GitHub**: `https://github.com/dajozu2513/Proyecto-VialReport.git`

---

## Current App State (what works)

- ✅ Login / Register / Logout with JWT auth
- ✅ Report list (citizen sees own, admin sees all) with search + status filters
- ✅ Create / Edit / Delete reports
- ✅ Photo upload with Gemini AI validation
- ✅ Admin can change report status from detail screen
- ✅ TopAppBar shows "VialReport" + "Hola, [name]" subtitle
- ✅ Firebase App Distribution set up (project: VialReport, package: com.vialreport.app)

## Pending Features (priority order)

1. **Map screen** — backend has `GET /map/points` and `GET /map/heatmap` ready
2. **Push notifications** — backend has `NotificationService` + notifications collection; needs FCM
3. **Admin stats panel** — backend has `GET /admin/stats` ready
4. **Crew management** — backend has `CrewRoutes` + `CrewService` complete
5. **Status change history** — backend already returns `statusLog` in report response; just needs UI
6. ~~**Phone number on register**~~ ✅ Done
5. ~~**Status change history**~~ ✅ Done
3. ~~**Admin stats panel**~~ ✅ Done
1. ~~**Map screen**~~ ✅ Done — `MapScreen` (osmdroid, círculos por estado), `GET /map/reports`

Remaining:
- **Push notifications** — backend `NotificationService` listo; necesita FCM en Android
- **Crew management** — backend `CrewRoutes` + `CrewService` completo; falta UI

---

## Frontend Architecture

Clean Architecture with MVVM under `app/src/main/java/com/vialreport/app/`:

- **`core/`** — Hilt app entry point (`VialReportApp`), DI modules (`NetworkModule`, `RepositoryModule`), `AuthInterceptor`
- **`data/local/`** — `TokenStore`: **primary auth state**; SharedPreferences (`vialreport_prefs`) storing `jwt_token`, `user_role`, `user_name`; exposes `isAdmin: Boolean` and `clear()`. (`TokenManager` is a legacy duplicate that only handles the token — use `TokenStore` everywhere.)
- **`data/remote/api/`** — `ReportApi`, `AuthApi`
- **`data/remote/dto/`** — `ApiResponseDto<T>`, `ReportDto` (includes `PhotoDto` list), `AuthDto`, `ReportRequestDto`, `UpdateStatusRequestDto`, `PhotoDto`
- **`data/remote/mapper/`** — `ReportMapper` (DTO → domain, maps nested photos)
- **`data/repository/`** — `ReportRepositoryImpl`, `AuthRepositoryImpl`
- **`domain/model/`** — `Report` (includes `photos: List<ReportPhoto>`), `ReportPhoto`, `IncidentType`, `User`
- **`domain/repository/`** — `IReportRepository`, `IAuthRepository`
- **`domain/usecase/auth/`** — `LoginUseCase` (returns `Result<User>`), `RegisterUseCase`, `IsLoggedInUseCase`, `LogoutUseCase`
- **`domain/usecase/report/`** — `GetAllReportsUseCase`, `GetReportByIdUseCase`, `CreateReportUseCase`, `UpdateReportUseCase` (citizen edits fields via `PUT /reports/{id}`), `UpdateStatusUseCase` (admin via `PUT /reports/{id}/status`), `DeleteReportUseCase`, `GetIncidentTypesUseCase`, `UploadPhotoUseCase`
- **`presentation/auth/login/`** — `LoginScreen`, `LoginViewModel`, `LoginUiState` (sealed: Idle/Loading/Success/Error). HTTP 401 shows "Correo o contraseña incorrectos"
- **`presentation/auth/register/`** — `RegisterScreen`, `RegisterViewModel`, `RegisterUiState`
- **`presentation/report/list/`** — list with search + status filter chips; TopAppBar shows name
- **`presentation/report/detail/`** — photos (horizontal scroll + upload), admin status change section
- **`presentation/report/form/`** — create/edit form with incident type dropdown from backend
- **`presentation/report/util/ReportDisplayUtils`** — pure display helpers: `statusLabel/Color()`, `typeLabel()`, `priorityLabel/Color()`; maps backend string values to Spanish UI labels and Material colors
- **`presentation/navigation/`** — `AppNavGraph` (takes `TokenStore`), `Routes`

**Auth flow**: Start → LOGIN if no token, LIST if token exists. After login/register: token + role + userName saved to `TokenStore`. `AuthInterceptor` injects `Authorization: Bearer <token>` on every request.

**List refresh**: Uses `savedStateHandle["refresh_list"]` — set to `true` after form save or admin status change, triggers `loadReports()`.

**Roles**:
- `citizen` — default on register; own reports only
- `admin` — all reports + status change; set in MongoDB Atlas → users collection → change `"role": "admin"` → user must re-login
- `crew_member` — defined but no frontend yet

**Photos**: Uploaded as multipart to `POST /reports/{id}/photos`. Gemini validates image is a real road incident. Displayed with Coil. Render storage is ephemeral (lost on redeploy).

**DI**: Hilt. `RepositoryModule` binds `IReportRepository` → `ReportRepositoryImpl` and `IAuthRepository` → `AuthRepositoryImpl`.

---

## Backend Architecture

Service-oriented with Repository pattern under `app/src/main/java/com/vialreport/backend/`:

`Application.kt` wires everything manually: `DatabaseFactory.init()` → repositories → services → `Routing`. No DI framework; all dependencies constructed inline.

- **`config/`** — `Database.kt` (MongoDB Atlas; seeds 8 `IncidentType` docs on first run), `Security.kt` (JWT)
- **`model/`** — `Report`, `User`, `Crew`, `IncidentType`, `Notification`, `ReportPhoto`, `ReportStatusLog`
- **`dto/`** — Request/response payloads; `ApiResponse<T>` generic wrapper
- **`repository/`** — MongoDB coroutine driver (no ORM): `ReportRepository`, `UserRepository`, `IncidentTypeRepository`, `CrewRepository`, `ReportPhotoRepository`, `ReportStatusLogRepository`, `NotificationRepository`
- **`service/`** — `ReportService`, `AuthService`, `PhotoService`, `PhotoAiService` (Gemini 1.5 Flash), `NotificationService`, `CrewService`, `MapService`, `AdminService`
- **`routes/`** — `ReportRoutes`, `AuthRoutes`, `CrewRoutes`, `NotificationRoutes`, `IncidentTypeRoutes`, `MapRoutes`, `AdminRoutes`
- **`util/`** — `Exceptions` (`NotFoundException`, `BadRequestException`, `UnauthorizedException`); `UserRole`, `ReportStatus` (with `isValid()` / `requiresAdmin()` helpers), `ReportPriority`

**Key REST endpoints**:
```
POST   /auth/register
POST   /auth/login
GET    /incident-types                — public
GET    /reports                       — citizen: own; admin: all (?status=&typeId=&zone=)
GET    /reports/{id}
POST   /reports                       — citizen; body: {typeId, title, description, latitude, longitude, address}
PUT    /reports/{id}                  — citizen edits own; admin edits any
PUT    /reports/{id}/status           — admin only; body: {status, note?}
DELETE /reports/{id}
POST   /reports/{id}/photos           — multipart, Gemini-validated
GET    /reports/{id}/photos           — admin only
GET    /uploads/{filename}            — static files
GET    /map/points                    — map markers
GET    /map/heatmap                   — heatmap data
GET    /admin/stats                   — admin statistics
GET    /notifications                 — user notifications
```

---

## Key Technology Versions

| Area | Tech | Version |
|------|------|---------|
| Frontend language | Kotlin | 2.0.21 |
| UI | Jetpack Compose BOM | 2025.08.00 |
| DI | Hilt | 2.57.1 |
| HTTP client | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Image loading | Coil | 2.7.0 |
| Navigation | Jetpack Navigation | 2.9.3 |
| Backend framework | Ktor | 2.3.7 |
| Database | MongoDB Atlas (Kotlin coroutine driver) | — |
| Auth | Auth0 JWT | 4.4.0 |
| AI photo filter | Google Gemini 1.5 Flash API | — |
| Android min SDK | — | 24 |
| Android target SDK | — | 36 |
| Java target (Frontend) | JVM 17 | — |
