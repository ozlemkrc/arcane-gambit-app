package com.example.arcane_gambit.ui.screens

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.arcane_gambit.UnityLauncher
import com.example.arcane_gambit.utils.SessionManager

@Composable
fun SpectatorScreen(navController: NavController) {
        val context = LocalContext.current
        val sessionManager = remember { SessionManager(context) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
          // Launch Unity activity when this screen is shown
        LaunchedEffect(Unit) {
            try {
                // Get the real user token from session manager
                val userToken = sessionManager.getToken()
                
                if (userToken == null) {
                    errorMessage = "No authentication token found. Please log in again."
                    return@LaunchedEffect
                }
                
                Log.d("SpectatorScreen", "Launching Unity with user token")
                
                // Launch our Unity launcher instead of Unity directly
                val intent = Intent(context, UnityLauncher::class.java).apply {
                    putExtra("user_token", userToken)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)
                
                // Close this activity after launching Unity
                if (context is Activity) {
                    context.finish()
                }
            } catch (e: Exception) {
                Log.e("SpectatorScreen", "Failed to launch Unity: ${e.message}")
                errorMessage = "Failed to launch AR experience: ${e.message}"
            }
        }
    

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B1F3B)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {            if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    fontSize = 16.sp,
                    color = Color.Red
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Check if it's an authentication error to show appropriate buttons
                if (errorMessage?.contains("authentication", ignoreCase = true) == true) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(onClick = { 
                            // Navigate back to login
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }) {
                            Text("Login Again")
                        }
                        
                        OutlinedButton(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                } else {
                    Button(onClick = { 
                        errorMessage = null
                        // Try launching Unity again using our wrapper
                        try {
                            // Get the real user token from session manager
                            val userToken = sessionManager.getToken()
                            
                            if (userToken == null) {
                                errorMessage = "No authentication token found. Please log in again."
                                return@Button
                            }
                            
                            val intent = Intent(context, UnityLauncher::class.java).apply {
                                putExtra("user_token", userToken)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            errorMessage = "Failed to launch AR experience: ${e.message}"
                        }
                    }) {
                        Text("Retry AR Launch")
                    }
                }
            } else {
                Text(
                    text = "Launching AR Spectator Mode...",
                    fontSize = 20.sp,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CircularProgressIndicator(color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}
