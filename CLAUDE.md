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

No `gradlew` in Frontend — always build from Android Studio.

### Backend (Ktor)

Run from `Backend/`:
```powershell
.\run-backend.ps1   # sets env vars + runs server (gitignored, contains credentials)
```

Or manually:
```powershell
$env:MONGODB_URI  = "mongodb+srv://..."
$env:JWT_SECRET   = "cualquier-string-secreto-largo"
$env:JWT_ISSUER   = "vialreport"
$env:JWT_AUDIENCE = "vialreport-users"
.\gradlew :app:run
```

Build a fat JAR (used by Render/Docker):
```powershell
.\gradlew :app:shadowJar
# Output: Backend/app/build/libs/app-all.jar
```

Build and run via Docker:
```powershell
docker build -t vialreport-backend .
docker run -p 8080:8080 `
  -e MONGODB_URI="..." -e JWT_SECRET="..." `
  -e JWT_ISSUER="vialreport" -e JWT_AUDIENCE="vialreport-users" `
  vialreport-backend
```

Backend reads all config from env vars: `MONGODB_URI`, `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, `GEMINI_API_KEY` (get from aistudio.google.com — las claves del nuevo AI Studio empiezan con `AQ.` y usan el header `x-goog-api-key`; el modelo es `gemini-flash-latest`), `UPLOAD_DIR` (default `./uploads`), `PORT` (default 8080).

### API Testing

`Backend/VialReport.postman_collection.json` — Postman collection covering all endpoints. Import into Postman and set a `baseUrl` variable (e.g. `http://localhost:8080` or the Render URL).

### Tests

Neither project has a configured test suite — only a placeholder `ExampleUnitTest` in the backend. There are no test commands to run.

**Backend deployed at**: `https://proyecto-vialreport-8it4.onrender.com/`
**GitHub**: `https://github.com/dajozu2513/Proyecto-VialReport.git`

---

## What Works (fully implemented)

- ✅ Login / Register (with optional phone) / Logout with JWT auth
- ✅ Report list — citizen sees own, admin sees all; search + status filter chips
- ✅ Create / Edit / Delete reports with GPS location (FusedLocationProviderClient + Geocoder reverse-geocoding for address auto-fill)
- ✅ Photo upload during report creation (picker in form, uploaded after save)
- ✅ Photo upload in detail screen + **delete photo** (X button overlay per photo)
- ✅ Gemini AI photo validation — model `gemini-flash-latest` via `x-goog-api-key` header + `v1beta` endpoint; acepta fotos reales de incidentes viales, rechaza cartoons/anime/obsceno/no relacionado; fail-closed on API errors
- ✅ Admin: change report status from detail screen
- ✅ Admin: stats panel (`AdminStatsScreen` — summary cards + breakdown bars)
- ✅ Status change history shown in detail screen (`StatusHistorySection`)
- ✅ Map screen (`MapScreen` — osmdroid, colored circle markers by status, tap for detail)
- ✅ Profile editing (`EditProfileScreen` — name + phone; updates TokenStore.userName)
- ✅ TopAppBar: "VialReport" + "Hola, [name]"; icons: Map 🗺️, Stats 📊 (admin only), Profile 👤, Logout; green primary background
- ✅ Firebase App Distribution set up (project: VialReport, package: com.vialreport.app)
- ✅ App icon: Noto Emoji 🛣️ motorway converted to adaptive vector drawable (`ic_launcher_foreground.xml` + `ic_launcher_background.xml`)
- ✅ Design tokens applied — Jade dark palette (`#2D6A4F` primary, `#F1EFE8` bg, full dark mode); `dynamicColor = false` so system wallpaper never overrides the theme

## Pending

- **Push notifications** — backend `NotificationService` + `notifications` collection ready; needs FCM integration in Android
- **Crew management** — backend `CrewRoutes` + `CrewService` complete; needs frontend UI

---

## Frontend Architecture

Clean Architecture + MVVM under `app/src/main/java/com/vialreport/app/`:

### Core / DI
- `core/app/VialReportApp` — `@HiltAndroidApp`; initializes osmdroid tile cache
- `core/di/NetworkModule` — `OkHttpClient` (AuthInterceptor + logging), `Retrofit` (base URL: Render), provides `ReportApi`, `AuthApi`, `AdminApi`, `MapApi`
- `core/di/RepositoryModule` — binds `IReportRepository → ReportRepositoryImpl`, `IAuthRepository → AuthRepositoryImpl`
- `core/interceptor/AuthInterceptor` — injects `Authorization: Bearer <token>` using `TokenStore`

### Data layer
- `data/local/TokenStore` — **primary auth state**; SharedPreferences (`vialreport_prefs`) storing `jwt_token`, `user_role`, `user_name`; exposes `isAdmin: Boolean` and `clear()`. (`TokenManager` is a legacy duplicate — ignore it, use `TokenStore` everywhere.)
- `data/remote/api/` — `ReportApi`, `AuthApi` (login, register, `GET /auth/me`, `PUT /auth/me`), `AdminApi` (`GET /admin/stats`), `MapApi` (`GET /map/reports`)
- `data/remote/dto/` — `ApiResponseDto<T>`, `ReportDto` (includes `StatusLogDto` list + `PhotoDto` list), `AuthDto` (includes `UpdateProfileRequestDto`, `UserDto` with phone), `AdminStatsDto`, `MapPointDto`, `StatusLogDto`
- `data/remote/mapper/ReportMapper` — DTO → domain; maps photos + statusLog
- `data/repository/` — `ReportRepositoryImpl` (includes `uploadPhoto`, `deletePhoto`), `AuthRepositoryImpl`

### Domain layer
- `domain/model/` — `Report` (photos + statusLog), `ReportPhoto`, `StatusLogEntry`, `IncidentType`, `User`
- `domain/repository/` — `IReportRepository` (includes `deletePhoto`), `IAuthRepository`
- `domain/usecase/auth/` — `LoginUseCase`, `RegisterUseCase` (phone param), `IsLoggedInUseCase`, `LogoutUseCase`, `UpdateProfileUseCase` (calls `PUT /auth/me`, updates `TokenStore.userName`)
- `domain/usecase/report/` — `GetAllReportsUseCase`, `GetReportByIdUseCase`, `CreateReportUseCase`, `UpdateReportUseCase`, `UpdateStatusUseCase`, `DeleteReportUseCase`, `GetIncidentTypesUseCase`, `UploadPhotoUseCase`, `DeletePhotoUseCase`
- `domain/usecase/admin/GetAdminStatsUseCase` — injects `AdminApi` directly
- `domain/usecase/map/GetMapPointsUseCase` — injects `MapApi` directly

### Presentation layer
- `presentation/auth/login/` — `LoginScreen`, `LoginViewModel`, `LoginUiState` (sealed: Idle/Loading/Success/Error)
- `presentation/auth/register/` — `RegisterScreen` (name, email, password, phone optional), `RegisterViewModel`, `RegisterUiState`
- `presentation/report/list/` — `ReportListScreen` (search + status filters; TopAppBar with Map/Stats/Profile/Logout icons), `ReportListViewModel`
- `presentation/report/detail/` — `ReportDetailScreen` (photos with X-delete per photo, status change for admin, `StatusHistorySection`), `ReportDetailViewModel`, `ReportDetailUiState` (includes `deletingPhotoId`)
- `presentation/report/form/` — `ReportFormScreen` (GPS location section with permission flow + Geocoder, photo picker with preview), `ReportFormViewModel` (saves report then uploads pending photo), `ReportFormUiState` (includes `LocationStatus` enum, `latitude/longitude: Double?`, `hasPendingPhoto`)
- `presentation/report/util/ReportDisplayUtils` — `statusLabel/Color()`, `typeLabel()`, `priorityLabel/Color()`
- `presentation/admin/` — `AdminStatsScreen`, `AdminStatsViewModel`, `AdminStatsUiState`
- `presentation/map/` — `MapScreen` (OSMDroid `AndroidView`, colored circle markers, bottom sheet on tap), `MapViewModel`, `MapUiState`
- `presentation/profile/` — `EditProfileScreen` (name + phone editable, email read-only), `EditProfileViewModel` (loads via `GET /auth/me`), `EditProfileUiState`
- `presentation/navigation/` — `AppNavGraph` (takes `TokenStore`); `Routes`: LOGIN, REGISTER, LIST, DETAIL, FORM, MAP, STATS, PROFILE

### Key patterns
- **Auth flow**: Start → LOGIN if no token, LIST if token exists
- **List refresh**: `savedStateHandle["refresh_list"] = true` after form save or status change
- **Location**: `FusedLocationProviderClient.getCurrentLocation()` in Screen coroutine scope; result passed to ViewModel via `onLocationObtained(lat, lng)`; Geocoder tries to auto-fill address
- **Photo during creation**: bytes stored in ViewModel field (`pendingPhotoBytes`); uploaded after `createReportUseCase` returns the new report ID
- **Roles**: `citizen` (default), `admin` (set in MongoDB Atlas → re-login required), `crew_member` (no frontend yet)
- **Permissions**: `INTERNET`, `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE` (≤API32), `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`

---

## Backend Architecture

Service-oriented + Repository pattern under `app/src/main/java/com/vialreport/backend/`:

`Application.kt` wires everything manually (no DI): `DatabaseFactory.init()` → repositories → services → `Routing`.

- **`config/`** — `Database.kt` (MongoDB Atlas; seeds 8 `IncidentType` docs on first run), `Security.kt` (JWT)
- **`model/`** — `Report` (has `citizenId`), `User` (has `phone`, `cedula`, `isVerified`), `Crew`, `IncidentType`, `Notification`, `ReportPhoto`, `ReportStatusLog`
- **`dto/ApiResponse.kt`** — `ApiResponse<T>`, `LoginRequest`, `RegisterRequest`, `AuthResponse`, `UserResponse`, `UpdateProfileRequest`
- **`repository/`** — MongoDB coroutine driver; `ReportPhotoRepository` has `findById()` + `deleteById()`
- **`service/`** — `PhotoService` (has `deletePhoto()` — deletes DB record + physical file), `PhotoAiService` (Gemini 1.5 Flash; fail-closed on API errors; ERROR log if key missing), others unchanged
- **`routes/`** — all routes; `ReportRoutes` has `DELETE /reports/{id}/photos/{photoId}`; `AuthRoutes` has `PUT /auth/me`

### All REST endpoints
```
POST   /auth/register
POST   /auth/login
GET    /auth/me                       — authenticated
PUT    /auth/me                       — body: {name, phone?}
GET    /incident-types                — public
GET    /reports                       — citizen: own; admin: all (?status=&typeId=&zone=)
GET    /reports/{id}                  — returns statusLog[] + photos[]
POST   /reports
PUT    /reports/{id}
PUT    /reports/{id}/status           — admin only; body: {status, note?}
DELETE /reports/{id}
POST   /reports/{id}/photos           — multipart, Gemini-validated
GET    /reports/{id}/photos           — admin only
DELETE /reports/{id}/photos/{photoId} — owner or admin
GET    /uploads/{filename}            — static files
GET    /map/reports                   — map markers (public, no auth required)
GET    /map/heatmap
GET    /admin/stats                   — admin only
GET    /notifications                 — user notifications
```

---

## Design System

Tokens defined in `Frontend/app/src/main/java/com/vialreport/app/ui/theme/`:

| Token | Light | Dark | Uso |
|-------|-------|------|-----|
| `primary` | `#2D6A4F` Jade oscuro | `#52A07A` | Botones, TopAppBar, FAB, chips seleccionados |
| `primaryContainer` | `#D0EBE0` Jade tint | `#2D6A4F` | Fondo de contenedores |
| `background` | `#F1EFE8` | `#1C1C1E` | Fondo de pantallas |
| `surface` | `#FFFFFF` | `#2C2C2E` | Cards, diálogos |
| `onSurfaceVariant` | `#5F5E5A` | `#B4B2A9` | Texto secundario |

- `dynamicColor = false` en `VialReportTheme` — el tema siempre usa los tokens, nunca el color del fondo de pantalla del usuario.
- Íconos de incidente: Inundación 🌊, Alumbrado público 💡, Basura acumulada 🗑️, Bache 🕳️, Señal dañada 🚧, Semáforo dañado 🚦, Derrumbe 🪨, Grieta en acera ⚠️

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
| Map | OSMDroid | 6.1.20 |
| Location | play-services-location | 21.3.0 |
| Backend framework | Ktor | 2.3.7 |
| Database | MongoDB Atlas (Kotlin coroutine driver) | — |
| Auth | Auth0 JWT | 4.4.0 |
| AI photo filter | Google Gemini 1.5 Flash API | — |
| Android min SDK | — | 24 |
| Android target SDK | — | 36 |
| Java target (Frontend) | JVM 17 | — |
