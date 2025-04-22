package com.example.arcane_gambit.ui.screens

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateCharacterScreen(
    onSaveCharacter: (String, Int) -> Unit, // Callback to save the character
    onBack: () -> Unit // Navigate back to the character list
) {
    var characterName by remember { mutableStateOf(TextFieldValue("")) }
    var characterLevel by remember { mutableStateOf(1) }  // Default level is 1
    var characterStrength by remember { mutableStateOf(10) } // Default strength
    var characterAgility by remember { mutableStateOf(10) } // Default agility

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B1F3B)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Your Character",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Character Name
            OutlinedTextField(
                value = characterName,
                onValueChange = { characterName = it },
                label = { Text("Character Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Character Level
            OutlinedTextField(
                value = characterLevel.toString(),
                onValueChange = { newValue ->
                    characterLevel = newValue.toIntOrNull() ?: characterLevel
                },
                label = { Text("Character Level") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Strength
            OutlinedTextField(
                value = characterStrength.toString(),
                onValueChange = { newValue ->
                    characterStrength = newValue.toIntOrNull() ?: characterStrength
                },
                label = { Text("Strength") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Agility
            OutlinedTextField(
                value = characterAgility.toString(),
                onValueChange = { newValue ->
                    characterAgility = newValue.toIntOrNull() ?: characterAgility
                },
                label = { Text("Agility") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    onSaveCharacter(characterName.text, characterLevel)
                    onBack()  // Go back after saving
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Character")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            TextButton(onClick = onBack) {
                Text("Back to Character List")
            }
        }
    }
}
