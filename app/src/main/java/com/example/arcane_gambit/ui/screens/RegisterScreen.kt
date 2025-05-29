package com.example.arcane_gambit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
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
import com.example.arcane_gambit.utils.SessionManager
import com.example.arcane_gambit.data.repository.AuthRepository
import com.example.arcane_gambit.data.repository.SettingsRepository
import com.example.arcane_gambit.ui.components.ServerSettingsDialog
import com.example.arcane_gambit.ui.components.SettingsButton
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit
) {    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var registrationSuccess by remember { mutableStateOf(false) }
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
                        colors = listOf(Color(0xFF1B1F3B), Color(0xFF4A4E69))                    )
                )
        ) {
            // Settings button in top-right corner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                SettingsButton(
                    onClick = { showSettingsDialog = true }
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
                    text = "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
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
                        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                            errorMessage = "All fields are required"
                            return@Button
                        }
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null                        // Simulate registration process
                        coroutineScope.launch {
                            try {
                                val result = authRepository.register(email, password)
                                result.fold(
                                    onSuccess = { authResponse ->
                                        // Save session with the received token
                                        sessionManager.createLoginSession(
                                            userId = "user_${System.currentTimeMillis()}", // Server doesn't return user ID
                                            username = email,
                                            token = authResponse.token
                                        )
                                        
                                        registrationSuccess = true
                                        isLoading = false
                                        
                                        // Navigate to login after a short delay
                                        kotlinx.coroutines.delay(1500)
                                        onLoginClick()
                                    },
                                    onFailure = { exception ->
                                        isLoading = false
                                        errorMessage = exception.message ?: "Registration failed"
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
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2E5B), // Button background color
                        contentColor = Color.White         // Button text/icon color
                    )
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = "Register", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onLoginClick) {
                    Text("Already have an account? Login", color = Color.White)
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

                // Registration success message
                if (registrationSuccess) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Registration successful! Redirecting to login...",
                        color = Color.Green,
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
            onDismiss = { showSettingsDialog = false },
            onSave = { config ->
                settingsRepository.setServerConfig(config)
                showSettingsDialog = false
                // Show confirmation message
                errorMessage = "Server settings saved successfully"
            }
        )
    }
}
