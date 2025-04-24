package com.example.arcane_gambit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc // Changed from NfcRounded
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
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    rfidTag: String? = null,
    isNfcEnabled: Boolean = false
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showRfidLogin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Remember rfidTag in a state variable so we can observe changes
    var currentRfidTag by remember { mutableStateOf(rfidTag) }

    // Update the rfidTag when it changes from outside
    LaunchedEffect(rfidTag) {
        currentRfidTag = rfidTag

        // Automatically switch to RFID login if tag is detected
        if (rfidTag != null && isNfcEnabled) {
            showRfidLogin = true
        }
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
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (showRfidLogin || currentRfidTag != null) {
                // Show RFID login UI
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Nfc, // Use Nfc instead of NfcRounded
                        contentDescription = "RFID Login",
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )

                    Text(
                        text = if (currentRfidTag != null)
                            "RFID Tag Detected!"
                        else
                            "Scan your RFID tag to login",
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    if (currentRfidTag != null) {
                        Text(
                            text = "RFID: ${currentRfidTag!!.take(8)}...",
                            color = Color.Green,
                            fontSize = 16.sp
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null

                                coroutineScope.launch {
                                    // In a real app, you would call your API to verify the RFID tag
                                    // For demo purposes, we'll simulate a successful login
                                    kotlinx.coroutines.delay(1000) // Simulate network delay

                                    // For now, let's use some mock data
                                    val mockUserId = "user_${currentRfidTag!!.hashCode()}"
                                    val mockUsername = "ArcaneHero"

                                    // Save session with RFID tag
                                    sessionManager.createLoginSession(
                                        userId = mockUserId,
                                        username = mockUsername,
                                        rfidTag = currentRfidTag,
                                        token = "mock_token_${System.currentTimeMillis()}"
                                    )

                                    isLoading = false
                                    onLoginSuccess()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Login with RFID")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { showRfidLogin = false }) {
                        Text("Use Username/Password Instead", color = Color.White)
                    }
                }
            } else {
                // Regular username/password login UI
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

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            errorMessage = "Username and password are required"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            // In a real app, you would call your API to verify credentials
                            // For demo purposes, we'll simulate a successful login with demo/password
                            kotlinx.coroutines.delay(1000) // Simulate network delay

                            if (username == "demo" && password == "password") {
                                // For demo, use mock data
                                val mockUserId = "user_123"

                                // Save session
                                sessionManager.createLoginSession(
                                    userId = mockUserId,
                                    username = username,
                                    rfidTag = currentRfidTag, // Link RFID tag if available
                                    token = "mock_token_${System.currentTimeMillis()}"
                                )

                                isLoading = false
                                onLoginSuccess()
                            } else {
                                isLoading = false
                                errorMessage = "Invalid username or password"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                if (isNfcEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { showRfidLogin = true }) {
                        Text("Login with RFID", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onRegisterClick) {
                    Text("Don't have an account? Register", color = Color.White)
                }
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