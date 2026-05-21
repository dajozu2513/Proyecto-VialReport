package com.vialreport.app.data.remote.api

import com.vialreport.app.data.remote.dto.ApiResponseDto
import com.vialreport.app.data.remote.dto.AuthResponseDto
import com.vialreport.app.data.remote.dto.LoginRequestDto
import com.vialreport.app.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponseDto<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): ApiResponseDto<AuthResponseDto>
}
