package com.example.arcane_gambit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.arcane_gambit.utils.SessionManager
import com.example.arcane_gambit.data.repository.AuthRepository
import com.example.arcane_gambit.data.repository.SettingsRepository
import com.example.arcane_gambit.ui.components.ServerSettingsDialog
import com.example.arcane_gambit.ui.components.SettingsButton
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val authRepository = remember { AuthRepository() }
    val settingsRepository = remember { SettingsRepository(context) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1B1F3B)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1B1F3B), Color(0xFF4A4E69))
                    )                )
        ) {
            // Settings button positioned at top-right
            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Server Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Regular email/password login UI
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Email and password are required"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val result = authRepository.login(email, password)
                                result.fold(
                                    onSuccess = { authResponse ->
                                        // Save session with the received token
                                        sessionManager.createLoginSession(
                                            userId = "user_${System.currentTimeMillis()}", // Server doesn't return user ID
                                            username = email,
                                            token = authResponse.token
                                        )
                                        
                                        isLoading = false
                                        onLoginSuccess()
                                    },
                                    onFailure = { exception ->
                                        isLoading = false
                                        errorMessage = exception.message ?: "Login failed"
                                    }
                                )
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Network error: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2E5B),
                        contentColor = Color.White  // This will make text and icon white
                    )
                ) {
                    Icon(Icons.Filled.Login, contentDescription = "Login", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onRegisterClick) {
                    Text("Don't have an account? Register", color = Color.White)
                }

                // Display error message if any
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Loading indicator
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
    
    // Settings Dialog
    if (showSettingsDialog) {
        ServerSettingsDialog(
            onDismiss = { showSettingsDialog = false },            onSave = { config ->
                Log.d("LoginScreen", "Saving server settings: ${config.ip}:${config.port}")
                settingsRepository.setServerConfig(config)
                showSettingsDialog = false
                // Show confirmation message
                errorMessage = "Server settings saved successfully"
            }
        )
    }
}}