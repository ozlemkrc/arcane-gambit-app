package com.example.arcane_gambit.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.arcane_gambit.data.repository.SettingsRepository

@Composable
fun ServerSettingsDialog(
    onDismiss: () -> Unit,
    onSave: (SettingsRepository.ServerConfig) -> Unit
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    
    var serverIp by remember { mutableStateOf(settingsRepository.getServerIp()) }
    var serverPort by remember { mutableStateOf(settingsRepository.getServerPort()) }
    var serverProtocol by remember { mutableStateOf(settingsRepository.getServerProtocol()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2E5B)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Server Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Protocol Selection
                Text(
                    text = "Protocol",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { serverProtocol = "http" },
                        label = { Text("HTTP") },
                        selected = serverProtocol == "http",
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4A4E69),
                            selectedLabelColor = Color.White,
                            labelColor = Color.Gray
                        )
                    )
                    FilterChip(
                        onClick = { serverProtocol = "https" },
                        label = { Text("HTTPS") },
                        selected = serverProtocol == "https",
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4A4E69),
                            selectedLabelColor = Color.White,
                            labelColor = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Server IP Field
                OutlinedTextField(
                    value = serverIp,
                    onValueChange = { 
                        serverIp = it
                        errorMessage = null
                    },
                    label = { Text("Server IP Address") },
                    placeholder = { Text("e.g., 192.168.1.100") },
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

                // Server Port Field
                OutlinedTextField(
                    value = serverPort,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() } && it.length <= 5) {
                            serverPort = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Server Port") },
                    placeholder = { Text("e.g., 3001") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Error message
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val config = SettingsRepository.ServerConfig(
                                ip = serverIp.trim(),
                                port = serverPort.trim(),
                                protocol = serverProtocol
                            )
                            
                            if (!config.isValid()) {
                                errorMessage = when {
                                    config.ip.isBlank() -> "IP address is required"
                                    config.port.isBlank() -> "Port is required"
                                    config.port.toIntOrNull() == null -> "Invalid port number"
                                    config.port.toInt() !in 1..65535 -> "Port must be between 1 and 65535"
                                    else -> "Invalid configuration"
                                }
                                return@Button
                            }
                            
                            Log.d("ServerSettings", "Saving server config: ${config.ip}:${config.port} (${config.protocol})")
                            onSave(config)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A4E69),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Reset to defaults button
                TextButton(
                    onClick = {
                        serverIp = "192.168.137.1"
                        serverPort = "3001"
                        serverProtocol = "http"
                        errorMessage = null
                    }
                ) {
                    Text("Reset to Defaults", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            Icons.Filled.Settings,
            contentDescription = "Server Settings",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}