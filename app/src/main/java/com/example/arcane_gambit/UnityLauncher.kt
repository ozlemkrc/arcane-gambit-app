package com.example.arcane_gambit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.arcane_gambit.utils.SessionManager

class UnityLauncher : Activity() {
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("UnityLauncher", "[DEBUG] UnityLauncher started")
        sessionManager = SessionManager(this)
        try {
            // Get token from SessionManager (which contains the server-authenticated token)
            val token = sessionManager.getToken()
            Log.d("UnityLauncher", "[DEBUG] Retrieved token from SessionManager: $token")
            if (token == null) {
                Log.e("UnityLauncher", "No user token available from SessionManager. User may not be logged in.")
                finish()
                return
            }
            Log.d("UnityLauncher", "[DEBUG] Launching Unity with server token: $token")
            // Store token for Unity to read as fallback
            storeTokenInSharedPreferences(token)
            // Launch Unity activity directly
            val unityIntent = Intent().apply {
                setClassName(packageName, "com.unity3d.player.UnityPlayerActivity")
                putExtra("user_token", token)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            Log.d("UnityLauncher", "[DEBUG] Sending token via Intent extra: $token")
            startActivity(unityIntent)
            // Send the token to Unity MainSceneManager using UnitySendMessage
            sendTokenToUnity(token)
            finish() // Close this launcher activity
        } catch (e: Exception) {
            Log.e("UnityLauncher", "Failed to launch Unity: ${e.message}")
            finish()
        }
    }
    
    private fun sendTokenToUnity(token: String) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        Log.d("UnityLauncher", "[DEBUG] Scheduling UnitySendMessage to send token in 3s: $token")
        // First attempt after 3 seconds to ensure Unity is loaded
        handler.postDelayed({
            attemptSendToken(token, 1, handler)
        }, 3000)
    }
    
    private fun attemptSendToken(token: String, attempt: Int, handler: android.os.Handler) {
        try {
            // Use reflection to call UnityPlayer.UnitySendMessage
            val unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer")
            val unitySendMessageMethod = unityPlayerClass.getMethod(
                "UnitySendMessage", 
                String::class.java, 
                String::class.java, 
                String::class.java
            )
            Log.d("UnityLauncher", "[DEBUG] Attempt $attempt: Sending token to Unity via UnitySendMessage: $token")
            // Call UnitySendMessage("MainSceneManager", "ReceiveToken", token)
            unitySendMessageMethod.invoke(null, "MainSceneManager", "ReceiveToken", token)
            Log.d("UnityLauncher", "Token sent to Unity MainSceneManager (attempt $attempt): $token")
        } catch (e: Exception) {
            Log.e("UnityLauncher", "Failed to send token to Unity MainSceneManager (attempt $attempt): ${e.message}")
            retryOrFallback(token, attempt, handler)
        }
    }
    
    private fun retryOrFallback(token: String, attempt: Int, handler: android.os.Handler) {
        if (attempt < 3) {
            val delay = attempt * 2000L // 4s, 6s
            Log.d("UnityLauncher", "[DEBUG] Retrying token send to MainSceneManager in ${delay}ms... (attempt ${attempt + 1})")
            handler.postDelayed({
                attemptSendToken(token, attempt + 1, handler)
            }, delay)
        } else {
            Log.w("UnityLauncher", "All attempts to send token to MainSceneManager via UnitySendMessage failed.")
            Log.i("UnityLauncher", "[DEBUG] Token is available via Intent extras and SharedPreferences for Unity to read. Token: $token")
        }
    }
    
    private fun storeTokenInSharedPreferences(token: String) {
        try {
            val sharedPref = getSharedPreferences("unity_bridge", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("user_token", token)
                putLong("token_timestamp", System.currentTimeMillis())
                apply()
            }
            Log.d("UnityLauncher", "[DEBUG] Token stored in SharedPreferences as fallback: $token")
        } catch (e: Exception) {
            Log.e("UnityLauncher", "Failed to store token in SharedPreferences: ${e.message}")
        }
    }
}