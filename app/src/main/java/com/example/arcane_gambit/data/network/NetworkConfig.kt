package com.example.arcane_gambit.data.network

import android.content.Context
import android.util.Log
import com.example.arcane_gambit.data.repository.SettingsRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkConfig {
    private var settingsRepository: SettingsRepository? = null
    
    fun initialize(context: Context) {
        settingsRepository = SettingsRepository(context)
    }
      fun getBaseUrl(): String {
        val config = settingsRepository?.getServerConfig() 
            ?: SettingsRepository.ServerConfig("192.168.137.1", "3001", "http")
        val baseUrl = config.getBaseUrl()
        Log.d("NetworkConfig", "Using base URL: $baseUrl")
        return baseUrl
    }
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
      private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Dynamically create Retrofit instance with current server settings
    val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
