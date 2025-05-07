package com.example.arcane_gambit.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterPageScreen(
    character: Character,
    onBackClick: () -> Unit,
    onJoinGameClick: (Character) -> Unit
) {
    // Calculate derived stats from character properties to match character creation stats
    val attackValue = character.strength * 2
    val defenceValue = (character.strength / 2) + (character.agility / 2) + 5
    val luckValue = character.agility * 3
    val vitalityValue = character.level * 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = character.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Gray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Appearance Placeholder",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = character.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Character Stats Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2F50)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Character Stats",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Divider(color = Color.White.copy(alpha = 0.2f))
                        
                        // Use the same stat format as character creation screen
                        StatProgressBar("Attack", attackValue)
                        StatProgressBar("Defence", defenceValue)
                        StatProgressBar("Luck", luckValue)
                        StatProgressBar("Vitality", vitalityValue)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Join Game Button
                Button(
                    onClick = { onJoinGameClick(character) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "JOIN GAME WITH ${character.name.uppercase()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = Color(0xFF1B1F3B)
    )
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun StatProgressBar(name: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row {
            // Show stat bars like in character creation screen
            // Maximum value is 25 (matches the slider in CreateCharacterScreen)
            val maxBars = 10
            val filledBars = (value.coerceAtMost(25) * maxBars / 25)
            
            for (i in 1..maxBars) {
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 14.dp)
                        .padding(horizontal = 1.dp)
                        .background(
                            color = if (i <= filledBars) 
                                getColorForStat(name) 
                            else 
                                Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = value.toString(),
                color = getColorForStat(name),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Helper function to get color for different stats
fun getColorForStat(statName: String): Color {
    return when (statName) {
        "Attack" -> Color(0xFFF44336)    // Red
        "Defence" -> Color(0xFF2196F3)   // Blue
        "Luck" -> Color(0xFFFFEB3B)      // Yellow
        "Vitality" -> Color(0xFF4CAF50)  // Green
        else -> Color.White
    }
}