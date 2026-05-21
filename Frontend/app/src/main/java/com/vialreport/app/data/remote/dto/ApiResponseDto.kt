package com.vialreport.app.data.remote.dto

data class ApiResponseDto<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
