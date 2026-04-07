package com.vialreport.backend.service

import com.vialreport.backend.dto.CrewRequest
import com.vialreport.backend.dto.CrewResponse
import com.vialreport.backend.repository.CrewRepository
import com.vialreport.backend.util.NotFoundException
import org.jetbrains.exposed.sql.transactions.transaction

class CrewService(
    private val crewRepository: CrewRepository
) {

    fun getAll(): List<CrewResponse> = transaction {
        crewRepository.findAll().map { it.toResponse() }
    }

    fun getById(id: Int): CrewResponse = transaction {
        crewRepository.findById(id)?.toResponse()
            ?: throw NotFoundException("Cuadrilla #$id no encontrada")
    }

    fun getAvailable(): List<CrewResponse> = transaction {
        crewRepository.findAvailable().map { it.toResponse() }
    }

    fun create(request: CrewRequest): CrewResponse = transaction {
        crewRepository.create(
            name = request.name,
            zone = request.zone
        ).toResponse()
    }

    fun setAvailability(id: Int, available: Boolean): CrewResponse = transaction {
        crewRepository.updateAvailability(id, available)?.toResponse()
            ?: throw NotFoundException("Cuadrilla #$id no encontrada")
    }

    fun delete(id: Int): Boolean = transaction {
        if (crewRepository.findById(id) == null) {
            throw NotFoundException("Cuadrilla #$id no encontrada")
        }
        crewRepository.delete(id)
    }
}