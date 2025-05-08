package com.example.arcane_gambit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.nfc.NfcAdapter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlaceholderScreen(
    character: Character,
    onBackClick: () -> Unit,
    onNfcDetected: (Character) -> Unit // Callback when NFC is detected
) {
    // Get the context for NFC adapter check
    val context = LocalContext.current

    // Check if NFC is available and enabled
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    val isNfcSupported = remember { nfcAdapter != null }
    val isNfcEnabled = remember { nfcAdapter?.isEnabled == true }

    // Calculate derived stats from character properties to match character creation stats
    val attackValue = character.strength * 2
    val defenceValue = (character.strength / 2) + (character.agility / 2) + 5
    val luckValue = character.agility * 3
    val vitalityValue = character.level * 5

    // Simulate NFC detection for UI preview if needed
    // This would typically be triggered by the MainActivity's NFC handling
    LaunchedEffect(Unit) {
        // In a real app, this won't be needed as MainActivity will handle actual NFC detection
        // This is just for UI demonstration when running in an emulator without NFC
        if (!isNfcSupported) {
            // Wait 3 seconds to simulate NFC detection
            kotlinx.coroutines.delay(3000)
            onNfcDetected(character)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game with ${character.name}",
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Character info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2F50)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = character.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Level ${character.level} Character",
                            fontSize = 16.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = if (isNfcSupported && isNfcEnabled) "Ready to join game"
                            else "NFC not available",
                            color = if (isNfcSupported && isNfcEnabled) Color.White.copy(alpha = 0.7f)
                            else Color(0xFFF44336),
                            fontSize = 14.sp
                        )
                    }
                }

                // NFC Section - Show appropriate UI based on NFC status
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2F50)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!isNfcSupported) {
                            // Device doesn't support NFC
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "NFC Not Supported",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(64.dp)
                            )

                            Text(
                                text = "NFC NOT SUPPORTED",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Your device doesn't support NFC, which is required to connect to the game station.",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        } else if (!isNfcEnabled) {
                            // NFC is supported but not enabled
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "NFC Disabled",
                                tint = Color(0xFFFFEB3B),
                                modifier = Modifier.size(64.dp)
                            )

                            Text(
                                text = "NFC IS DISABLED",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Please enable NFC in your device settings to connect to the game station.",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    // Open NFC settings
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFEB3B),
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("ENABLE NFC")
                            }
                        } else {
                            // Ready to connect - default state
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "NFC Icon",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(64.dp)
                            )

                            Text(
                                text = "TAP YOUR NFC DEVICE",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Tap your phone to the game station to join the game with ${character.name}",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Loading indicator - only show when waiting for NFC and it's enabled
                if (isNfcSupported && isNfcEnabled) {
                    CircularProgressIndicator(
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        text = "Waiting for NFC connection...",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Stats summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2F50)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Character Stats",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Divider(color = Color.White.copy(alpha = 0.2f))

                        // More compact layout with all stats in one row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CompactStatItem("ATK", attackValue.toString(), Color(0xFFF44336))
                            CompactStatItem("DEF", defenceValue.toString(), Color(0xFF2196F3))
                            CompactStatItem("LUCK", luckValue.toString(), Color(0xFFFFEB3B))
                            CompactStatItem("VIT", vitalityValue.toString(), Color(0xFF4CAF50))
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF1B1F3B)
    )
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun CompactStatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp) // Reduced spacing for compactness
    ) {
        Text(
            text = label,
            fontSize = 12.sp, // Slightly smaller font size
            color = Color.White.copy(alpha = 0.7f)
        )

        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) // Reduced corner radius
                .padding(horizontal = 8.dp, vertical = 4.dp) // Reduced padding
        ) {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp // Slightly smaller font size
            )
        }
    }
}