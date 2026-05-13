package com.vialreport.backend.service

import com.vialreport.backend.dto.CrewRequest
import com.vialreport.backend.dto.CrewResponse
import com.vialreport.backend.repository.CrewRepository
import com.vialreport.backend.util.NotFoundException

class CrewService(
    private val crewRepository: CrewRepository
) {

    suspend fun getAll(): List<CrewResponse> =
        crewRepository.findAll().map { it.toResponse() }

    suspend fun getById(id: String): CrewResponse =
        crewRepository.findById(id)?.toResponse()
            ?: throw NotFoundException("Cuadrilla $id no encontrada")

    suspend fun getAvailable(): List<CrewResponse> =
        crewRepository.findAvailable().map { it.toResponse() }

    suspend fun create(request: CrewRequest): CrewResponse =
        crewRepository.create(name = request.name, zone = request.zone).toResponse()

    suspend fun setAvailability(id: String, available: Boolean): CrewResponse =
        crewRepository.updateAvailability(id, available)?.toResponse()
            ?: throw NotFoundException("Cuadrilla $id no encontrada")

    suspend fun delete(id: String): Boolean {
        crewRepository.findById(id) ?: throw NotFoundException("Cuadrilla $id no encontrada")
        return crewRepository.delete(id)
    }
}
