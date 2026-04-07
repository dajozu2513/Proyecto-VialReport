package com.vialreport.app.domain.repository

import com.vialreport.app.domain.model.Report

interface IReportRepository {

    suspend fun getAll(): List<Report>

    suspend fun getById(id: String): Report?

    suspend fun create(
        title: String,
        description: String,
        type: String,
        status: String,
        priority: String,
        address: String,
        latitude: Double,
        longitude: Double,
        citizenName: String
    ): Report

    suspend fun update(
        id: String,
        title: String,
        description: String,
        type: String,
        status: String,
        priority: String,
        address: String,
        latitude: Double,
        longitude: Double,
        citizenName: String
    ): Report

    suspend fun delete(id: String): Boolean
}
