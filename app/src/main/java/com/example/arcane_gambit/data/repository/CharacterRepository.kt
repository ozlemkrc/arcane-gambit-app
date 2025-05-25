package com.example.arcane_gambit.data.repository

import com.example.arcane_gambit.data.model.CreateCharacterRequest
import com.example.arcane_gambit.data.model.ErrorResponse
import com.example.arcane_gambit.data.model.ServerCharacter
import com.example.arcane_gambit.data.network.CharacterApiService
import com.example.arcane_gambit.data.network.NetworkConfig
import com.google.gson.Gson

class CharacterRepository {
    private val characterApiService = NetworkConfig.retrofit.create(CharacterApiService::class.java)
    private val gson = Gson()
    
    suspend fun getAllCharacters(token: String): Result<List<ServerCharacter>> {
        return try {
            val response = characterApiService.getAllCharacters("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { characters ->
                    Result.success(characters)
                } ?: Result.success(emptyList())
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    gson.fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "Failed to fetch characters: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createCharacter(token: String, character: CreateCharacterRequest): Result<ServerCharacter> {
        return try {
            val response = characterApiService.createCharacter("Bearer $token", character)
            if (response.isSuccessful) {
                response.body()?.let { createdCharacter ->
                    Result.success(createdCharacter)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    gson.fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "Failed to create character: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCharacter(token: String, characterId: String): Result<Unit> {
        return try {
            val response = characterApiService.deleteCharacter("Bearer $token", characterId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    gson.fromJson(errorBody, ErrorResponse::class.java).message
                } catch (e: Exception) {
                    "Failed to delete character: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
