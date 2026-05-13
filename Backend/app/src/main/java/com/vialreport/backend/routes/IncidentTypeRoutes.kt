package com.vialreport.backend.routes

import com.vialreport.backend.dto.ApiResponse
import com.vialreport.backend.repository.IncidentTypeRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.incidentTypeRoutes(incidentTypeRepository: IncidentTypeRepository) {

    // GET /incident-types — público, sin auth (dropdown del app)
    get("/incident-types") {
        val types = incidentTypeRepository.findAll().map { it.toResponse() }
        call.respond(
            HttpStatusCode.OK,
            ApiResponse(success = true, message = "OK", data = types)
        )
    }
}
