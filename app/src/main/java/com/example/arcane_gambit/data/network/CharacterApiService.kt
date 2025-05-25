package com.example.arcane_gambit.data.network

import com.example.arcane_gambit.data.model.CreateCharacterRequest
import com.example.arcane_gambit.data.model.ServerCharacter
import retrofit2.Response
import retrofit2.http.*

interface CharacterApiService {
    @GET("characters")
    suspend fun getAllCharacters(@Header("Authorization") token: String): Response<List<ServerCharacter>>
    
    @GET("characters/{id}")
    suspend fun getCharacterById(
        @Header("Authorization") token: String,
        @Path("id") characterId: String
    ): Response<ServerCharacter>
    
    @POST("characters")
    suspend fun createCharacter(
        @Header("Authorization") token: String,
        @Body character: CreateCharacterRequest
    ): Response<ServerCharacter>
    
    @DELETE("characters/{id}")
    suspend fun deleteCharacter(
        @Header("Authorization") token: String,
        @Path("id") characterId: String
    ): Response<Unit>
}
