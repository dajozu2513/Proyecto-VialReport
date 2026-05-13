package com.vialreport.backend.routes

import com.vialreport.backend.dto.ApiResponse
import com.vialreport.backend.service.MapService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.mapRoutes(mapService: MapService) {

    route("/map") {

        get("/heatmap") {
            val typeId = call.request.queryParameters["typeId"]
            val zone   = call.request.queryParameters["zone"]
            val status = call.request.queryParameters["status"]
            val points = mapService.getHeatmap(typeId, zone, status)
            call.respond(HttpStatusCode.OK,
                ApiResponse(success = true, message = "OK", data = points))
        }

        get("/reports") {
            val typeId = call.request.queryParameters["typeId"]
            val zone   = call.request.queryParameters["zone"]
            val points = mapService.getMapPoints(typeId, zone)
            call.respond(HttpStatusCode.OK,
                ApiResponse(success = true, message = "OK", data = points))
        }
    }
}
