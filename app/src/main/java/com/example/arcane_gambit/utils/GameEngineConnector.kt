package com.example.arcane_gambit.utils

import android.util.Log
import com.example.arcane_gambit.ui.screens.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * A utility class to handle communication with the game engine
 * This class provides methods to connect to the game engine, send character data,
 * and retrieve game status information.
 */
class GameEngineConnector {

    // Configuration
    private val baseUrl = "https://arcane-gambit-engine.example.com/api"
    private var sessionToken: String? = null
    private var isConnected = false

    // Game session info
    private var gameSessionId: String? = null
    private var playerId: String? = null

    /**
     * Connect to the game engine server
     * @return true if connection successful, false otherwise
     */
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to connect to game engine...")

            // For development/testing, simulate a successful connection
            if (SIMULATE_RESPONSES) {
                delay(1500) // Simulate network delay
                isConnected = true
                sessionToken = "sim_token_${System.currentTimeMillis()}"
                Log.d(TAG, "Connected to game engine with simulated token: $sessionToken")
                return@withContext true
            }

            // Real implementation would connect to the game engine
            val url = URL("$baseUrl/connect")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            // Create request body
            val requestBody = JSONObject().apply {
                put("client_version", CLIENT_VERSION)
                put("platform", "android")
                put("timestamp", System.currentTimeMillis())
            }

            // Send request
            val outputStream = connection.outputStream
            outputStream.write(requestBody.toString().toByteArray(StandardCharsets.UTF_8))
            outputStream.close()

            // Check response
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                if (jsonResponse.optBoolean("success", false)) {
                    sessionToken = jsonResponse.optString("session_token")
                    isConnected = true
                    Log.d(TAG, "Connected to game engine successfully")
                    return@withContext true
                }
            }

            Log.e(TAG, "Failed to connect to game engine. Response code: ${connection.responseCode}")
            return@withContext false

        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to game engine: ${e.message}")
            isConnected = false
            return@withContext false
        }
    }

    /**
     * Disconnect from the game engine
     * @return true if disconnection successful, false otherwise
     */
    suspend fun disconnect(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isConnected) {
                return@withContext true // Already disconnected
            }

            Log.d(TAG, "Disconnecting from game engine...")

            // For development/testing, simulate a successful disconnection
            if (SIMULATE_RESPONSES) {
                delay(500) // Simulate network delay
                isConnected = false
                sessionToken = null
                gameSessionId = null
                playerId = null
                Log.d(TAG, "Disconnected from game engine (simulated)")
                return@withContext true
            }

            // Real implementation would disconnect from the game engine
            val url = URL("$baseUrl/disconnect")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $sessionToken")

            // Create request body
            val requestBody = JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
            }

            // Send request
            val outputStream = connection.outputStream
            outputStream.write(requestBody.toString().toByteArray(StandardCharsets.UTF_8))
            outputStream.close()

            // Check response
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                isConnected = false
                sessionToken = null
                gameSessionId = null
                playerId = null
                Log.d(TAG, "Disconnected from game engine successfully")
                return@withContext true
            }

            Log.e(TAG, "Failed to disconnect from game engine. Response code: ${connection.responseCode}")
            return@withContext false

        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from game engine: ${e.message}")
            // Consider the connection broken if an exception occurs during disconnection
            isConnected = false
            sessionToken = null
            gameSessionId = null
            playerId = null
            return@withContext false
        }
    }

    /**
     * Send character data to the game engine
     * @param character The character to send
     * @return true if data sent successfully, false otherwise
     */
    suspend fun sendCharacterData(character: Character): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected || sessionToken == null) {
            Log.e(TAG, "Cannot send character data: Not connected to game engine")
            return@withContext false
        }

        try {
            Log.d(TAG, "Sending character data to game engine: ${character.name}")

            // For development/testing, simulate a successful data transfer
            if (SIMULATE_RESPONSES) {
                // Simulate network delay
                delay(2000)
                playerId = "player_${System.currentTimeMillis()}"
                Log.d(TAG, "Character data sent successfully (simulated)")
                return@withContext true
            }

            // Real implementation would send character data to the game engine
            val url = URL("$baseUrl/characters")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $sessionToken")

            // Create request body with character data
            val requestBody = JSONObject().apply {
                put("character_id", character.id)
                put("name", character.name)
                put("level", character.level)
                put("strength", character.strength)
                put("agility", character.agility)

                // Calculated stats
                put("attack", character.strength * 2)
                put("defence", (character.strength / 2) + (character.agility / 2) + 5)
                put("luck", character.agility * 3)
                put("vitality", character.level * 5)

                put("timestamp", System.currentTimeMillis())
            }

            // Send request
            val outputStream = connection.outputStream
            outputStream.write(requestBody.toString().toByteArray(StandardCharsets.UTF_8))
            outputStream.close()

            // Check response
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                if (jsonResponse.optBoolean("success", false)) {
                    playerId = jsonResponse.optString("player_id")
                    Log.d(TAG, "Character data sent successfully")
                    return@withContext true
                }
            }

            Log.e(TAG, "Failed to send character data. Response code: ${connection.responseCode}")
            return@withContext false

        } catch (e: Exception) {
            Log.e(TAG, "Error sending character data: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Join a game session
     * @param sessionId Optional specific session ID to join, or null to join any available session
     * @return true if joined successfully, false otherwise
     */
    suspend fun joinGameSession(sessionId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected || sessionToken == null || playerId == null) {
            Log.e(TAG, "Cannot join game session: Not properly connected or no character data sent")
            return@withContext false
        }

        try {
            val targetSession = sessionId ?: "auto"
            Log.d(TAG, "Attempting to join game session: $targetSession")

            // For development/testing, simulate a successful join
            if (SIMULATE_RESPONSES) {
                delay(1500) // Simulate network delay
                gameSessionId = sessionId ?: "session_${System.currentTimeMillis()}"
                Log.d(TAG, "Joined game session successfully (simulated): $gameSessionId")
                return@withContext true
            }

            // Real implementation would join a game session
            val url = URL("$baseUrl/sessions/join")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $sessionToken")

            // Create request body
            val requestBody = JSONObject().apply {
                put("player_id", playerId)
                if (sessionId != null) {
                    put("session_id", sessionId)
                }
                put("timestamp", System.currentTimeMillis())
            }

            // Send request
            val outputStream = connection.outputStream
            outputStream.write(requestBody.toString().toByteArray(StandardCharsets.UTF_8))
            outputStream.close()

            // Check response
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                if (jsonResponse.optBoolean("success", false)) {
                    gameSessionId = jsonResponse.optString("session_id")
                    Log.d(TAG, "Joined game session successfully: $gameSessionId")
                    return@withContext true
                }
            }

            Log.e(TAG, "Failed to join game session. Response code: ${connection.responseCode}")
            return@withContext false

        } catch (e: Exception) {
            Log.e(TAG, "Error joining game session: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Check if a game session is active
     * @return true if the session is active, false otherwise
     */
    suspend fun isGameSessionActive(): Boolean = withContext(Dispatchers.IO) {
        if (!isConnected || sessionToken == null) {
            return@withContext false
        }

        // For development/testing, always return true with a chance of false
        if (SIMULATE_RESPONSES) {
            delay(500) // Simulate network delay
            val isActive = Math.random() > 0.2 // 80% chance of active session
            Log.d(TAG, "Game session active check (simulated): $isActive")
            return@withContext isActive
        }

        try {
            val sessionIdToCheck = gameSessionId ?: return@withContext false

            val url = URL("$baseUrl/sessions/$sessionIdToCheck/status")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $sessionToken")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                return@withContext jsonResponse.optBoolean("active", false)
            }

            Log.e(TAG, "Failed to check game session status. Response code: ${connection.responseCode}")
            return@withContext false

        } catch (e: Exception) {
            Log.e(TAG, "Error checking game session status: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Get game status information
     * @return Map of game status information or empty map if not available
     */
    suspend fun getGameStatus(): Map<String, Any> = withContext(Dispatchers.IO) {
        if (!isConnected || sessionToken == null) {
            return@withContext emptyMap()
        }

        // For development/testing, return simulated game status
        if (SIMULATE_RESPONSES) {
            delay(1000) // Simulate network delay
            return@withContext mapOf(
                "gameMode" to listOf("Standard", "Capture the Flag", "Team Deathmatch").random(),
                "playersConnected" to (1..4).random(),
                "maxPlayers" to 6,
                "mapName" to listOf("Mystic Forest", "Arcane Tower", "Shadow Caverns", "Dragon's Lair").random(),
                "gameSessionId" to (gameSessionId ?: "session_${System.currentTimeMillis()}"),
                "timeLeft" to "${(5..30).random()} minutes"
            )
        }

        try {
            val sessionIdToCheck = gameSessionId ?: return@withContext emptyMap()

            val url = URL("$baseUrl/sessions/$sessionIdToCheck/details")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $sessionToken")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(response)

                val result = mutableMapOf<String, Any>()

                // Extract game details from response
                if (jsonResponse.has("game_mode")) result["gameMode"] = jsonResponse.getString("game_mode")
                if (jsonResponse.has("players_connected")) result["playersConnected"] = jsonResponse.getInt("players_connected")
                if (jsonResponse.has("max_players")) result["maxPlayers"] = jsonResponse.getInt("max_players")
                if (jsonResponse.has("map_name")) result["mapName"] = jsonResponse.getString("map_name")
                if (jsonResponse.has("session_id")) result["gameSessionId"] = jsonResponse.getString("session_id")
                if (jsonResponse.has("time_left")) result["timeLeft"] = jsonResponse.getString("time_left")

                return@withContext result
            }

            Log.e(TAG, "Failed to get game status. Response code: ${connection.responseCode}")
            return@withContext emptyMap()

        } catch (e: Exception) {
            Log.e(TAG, "Error getting game status: ${e.message}")
            return@withContext emptyMap()
        }
    }

    /**
     * Check if currently connected to game engine
     */
    fun isConnected(): Boolean {
        return isConnected && sessionToken != null
    }

    /**
     * Check if a player is in a game session
     */
    fun isInGameSession(): Boolean {
        return isConnected && sessionToken != null && gameSessionId != null && playerId != null
    }

    companion object {
        private const val TAG = "GameEngineConnector"
        private const val CLIENT_VERSION = "1.0.0"

        // Set to true for development/testing without a real server
        // Set to false when you have a real game engine server to connect to
        private const val SIMULATE_RESPONSES = true
    }
}