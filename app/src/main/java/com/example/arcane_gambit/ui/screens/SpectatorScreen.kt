package com.example.arcane_gambit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment

@Composable
fun SpectatorScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B1F3B)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AR Spectator Mode Coming Soon",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}
