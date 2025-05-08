package com.example.arcane_gambit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
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
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Arcane Gambit",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2E5B),
                        contentColor = Color.White  // This sets the text and icon color to white
                    )
                ) {
                    Icon(Icons.Filled.Login, contentDescription = "Login", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = "Register", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register")
                }

                
            }
        }
    }
}
