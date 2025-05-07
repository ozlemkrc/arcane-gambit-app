package com.example.arcane_gambit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SpectatorScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B1F3B)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AR Spectator Mode Coming Soon",
                fontSize = 20.sp,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}
