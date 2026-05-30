package com.vialreport.app.data.remote.api

import com.vialreport.app.data.remote.dto.ApiResponseDto
import com.vialreport.app.data.remote.dto.AuthResponseDto
import com.vialreport.app.data.remote.dto.LoginRequestDto
import com.vialreport.app.data.remote.dto.RegisterRequestDto
import com.vialreport.app.data.remote.dto.UpdateProfileRequestDto
import com.vialreport.app.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponseDto<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): ApiResponseDto<AuthResponseDto>

    @GET("auth/me")
    suspend fun getMe(): ApiResponseDto<UserDto>

    @PUT("auth/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): ApiResponseDto<UserDto>
}
