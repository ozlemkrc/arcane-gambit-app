package com.example.arcane_gambit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc  // Changed from NfcRounded
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arcane_gambit.utils.SessionManager
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    rfidTag: String? = null,
    isNfcEnabled: Boolean = false
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Remember rfidTag in a state variable so we can observe changes
    var currentRfidTag by remember { mutableStateOf(rfidTag) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var registrationSuccess by remember { mutableStateOf(false) }

    // Update the rfidTag when it changes from outside
    LaunchedEffect(rfidTag) {
        currentRfidTag = rfidTag
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B1F3B)) {
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
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
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
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
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

            // RFID Section
            if (isNfcEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2E5B))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Nfc,  // Changed from NfcRounded
                                contentDescription = "RFID Tag",
                                tint = Color.White
                            )

                            Text(
                                text = "RFID Tag (Optional)",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentRfidTag != null) {
                            Text(
                                text = "RFID Tag Detected: ${currentRfidTag!!.take(8)}...",
                                color = Color.Green,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "Scan your RFID tag to link it with your account",
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Validate inputs
                    when {
                        username.isBlank() -> errorMessage = "Username is required"
                        email.isBlank() -> errorMessage = "Email is required"
                        !email.contains("@") -> errorMessage = "Invalid email format"
                        password.isBlank() -> errorMessage = "Password is required"
                        password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                        password != confirmPassword -> errorMessage = "Passwords do not match"
                        else -> {
                            isLoading = true
                            errorMessage = null

                            coroutineScope.launch {
                                // In a real app, you would call your API to register the user
                                // For demo purposes, we'll simulate a successful registration
                                kotlinx.coroutines.delay(1500) // Simulate network delay

                                try {
                                    // Mock successful registration
                                    val mockUserId = "user_${System.currentTimeMillis()}"

                                    // Store registration data in SessionManager
                                    // In a real app, this would happen after successful API registration
                                    sessionManager.createLoginSession(
                                        userId = mockUserId,
                                        username = username,
                                        rfidTag = currentRfidTag
                                    )

                                    isLoading = false
                                    registrationSuccess = true

                                    // Show success message briefly before navigating
                                    kotlinx.coroutines.delay(1000)
                                    onLoginClick()
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Registration failed: ${e.message}"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !registrationSuccess
            ) {
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