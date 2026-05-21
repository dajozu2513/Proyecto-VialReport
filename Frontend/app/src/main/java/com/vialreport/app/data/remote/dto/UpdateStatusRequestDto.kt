package com.vialreport.app.data.remote.dto

data class UpdateStatusRequestDto(
    val status: String,
    val note: String? = null
)
