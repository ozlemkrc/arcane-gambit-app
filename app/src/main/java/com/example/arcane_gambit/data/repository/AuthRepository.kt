package com.example.arcane_gambit.data.repository

import com.example.arcane_gambit.data.model.AuthResponse
import com.example.arcane_gambit.data.model.LoginRequest
import com.example.arcane_gambit.data.model.RegisterRequest
import com.example.arcane_gambit.data.network.AuthApiService
import com.example.arcane_gambit.data.network.NetworkConfig
import com.google.gson.Gson
import com.example.arcane_gambit.data.model.ErrorResponse

class AuthRepository {
    private val gson = Gson()
    
    // Get fresh API service instance to use current server settings
    private fun getAuthApiService() = NetworkConfig.retrofit.create(AuthApiService::class.java)
      suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = getAuthApiService().login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    gson.fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "Login failed: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
      suspend fun register(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = getAuthApiService().register(RegisterRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    Result.success(authResponse)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    gson.fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "Registration failed: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
