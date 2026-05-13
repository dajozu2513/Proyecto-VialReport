package com.vialreport.backend

import com.vialreport.backend.config.DatabaseFactory
import com.vialreport.backend.config.configureSecurity
import com.vialreport.backend.repository.*
import com.vialreport.backend.routes.*
import com.vialreport.backend.service.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {

    // ── 1. Base de datos ──────────────────────────────────────
    DatabaseFactory.init(environment)

    // ── 2. JSON ───────────────────────────────────────────────
    install(ContentNegotiation) {
        json(Json {
            prettyPrint        = true
            isLenient          = true
            ignoreUnknownKeys  = true
        })
    }

    // ── 3. CORS ───────────────────────────────────────────────
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }

    // ── 4. Manejo global de errores ───────────────────────────
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Error interno"))
            )
        }
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "No autorizado")
            )
        }
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Recurso no encontrado")
            )
        }
    }

    // ── 5. JWT ────────────────────────────────────────────────
    configureSecurity()

    // ── 6. Dependencias ──────────────────────────────────────
    val jwtSecret   = environment.config.property("jwt.secret").getString()
    val jwtIssuer   = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()

    val userRepository        = UserRepository()
    val reportRepository      = ReportRepository()
    val incidentTypeRepository = IncidentTypeRepository()
    val crewRepository        = CrewRepository()
    val photoRepository       = ReportPhotoRepository()
    val statusLogRepository   = ReportStatusLogRepository()
    val notificationRepository = NotificationRepository()

    val anthropicKey = System.getenv("ANTHROPIC_API_KEY") ?: ""
    val uploadDir    = System.getenv("UPLOAD_DIR") ?: "./uploads"

    val notificationService = NotificationService(notificationRepository)
    val authService         = AuthService(userRepository, jwtSecret, jwtIssuer, jwtAudience)
    val reportService       = ReportService(
        reportRepository,
        userRepository,
        incidentTypeRepository,
        crewRepository,
        photoRepository,
        statusLogRepository,
        notificationService
    )
    val crewService    = CrewService(crewRepository)
    val photoAiService = PhotoAiService(anthropicKey)
    val photoService   = PhotoService(reportRepository, photoRepository, photoAiService, uploadDir)
    val mapService     = MapService(reportRepository)
    val adminService   = AdminService(reportRepository)

    // ── 7. Rutas ──────────────────────────────────────────────
    install(Routing) {
        get("/") {
            call.respond(mapOf("status" to "VialReport API corriendo ✓"))
        }

        authRoutes(authService, userRepository)
        reportRoutes(reportService, photoService)
        crewRoutes(crewService)
        notificationRoutes(notificationService)
        incidentTypeRoutes(incidentTypeRepository)
        mapRoutes(mapService)
        adminRoutes(adminService)
    }
}