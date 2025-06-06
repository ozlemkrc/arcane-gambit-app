package com.example.arcane_gambit.data.network

import com.example.arcane_gambit.data.model.AuthResponse
import com.example.arcane_gambit.data.model.LoginRequest
import com.example.arcane_gambit.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}
