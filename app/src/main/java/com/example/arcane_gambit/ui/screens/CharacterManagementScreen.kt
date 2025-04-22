package com.example.arcane_gambit.ui.screens

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CharacterManagementScreen(
    onCreateCharacterClick: () -> Unit,
    onCharacterClick: (characterId: String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1B1F3B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your Characters",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Display character list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { index ->  // Placeholder for 5 characters
                    CharacterCard(
                        characterName = "Character $index",
                        characterLevel = 1 + index,
                        onClick = { onCharacterClick("Character $index") }
                    )
                }
            }

            // Floating Action Button for creating a new character
            FloatingActionButton(
                onClick = onCreateCharacterClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("+", color = Color.White, fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun CharacterCard(
    characterName: String,
    characterLevel: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2E5B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = characterName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Level: $characterLevel",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            // Optionally, an edit or delete icon can be added here later.
        }
    }
}
