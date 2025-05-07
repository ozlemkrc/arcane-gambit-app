package com.example.arcane_gambit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

data class CharacterClass(
    val name: String,
    val description: String,
    val icon: ImageVector, // Changed to use ImageVector instead of resourceId
    val baseStats: CharacterStats
)

data class CharacterStats(
    val attack: Int,
    val defence: Int,
    val luck: Int,
    val vitality: Int
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CharacterSelectionScreen(
    onSaveCharacter: (String, CharacterStats) -> Unit,
    onBack: () -> Unit
) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1B1F3B), Color(0xFF4A4E69))
    )
    
    // Define character classes with default stats and Material icons
    val characterClasses = listOf(
        CharacterClass(
            name = "Warrior",
            description = "A mighty fighter skilled in close combat",
            icon = Icons.Filled.Shield, // Using Material icon instead
            baseStats = CharacterStats(attack = 15, defence = 18, luck = 8, vitality = 20)
        ),
        CharacterClass(
            name = "Mage",
            description = "A master of arcane arts and powerful spells",
            icon = Icons.Filled.Bolt, // Using Material icon instead
            baseStats = CharacterStats(attack = 20, defence = 8, luck = 12, vitality = 15)
        ),
        CharacterClass(
            name = "Archer",
            description = "A precise marksman with deadly accuracy",
            icon = Icons.Filled.Report, // Using Material icon instead
            baseStats = CharacterStats(attack = 18, defence = 10, luck = 15, vitality = 12)
        )
    )
    
    var selectedClassIndex by remember { mutableIntStateOf(-1) }
    var isEditMode by remember { mutableStateOf(false) }
    var characterName by remember { mutableStateOf("") }
    
    val pagerState = rememberPagerState()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            AnimatedVisibility(
                visible = !isEditMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose Your Character",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                    
                    HorizontalPager(
                        count = characterClasses.size,
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        CharacterCard(
                            characterClass = characterClasses[page],
                            isSelected = selectedClassIndex == page,
                            onClick = {
                                selectedClassIndex = page
                                isEditMode = true
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier.padding(16.dp),
                        activeColor = Color.White,
                        inactiveColor = Color.Gray
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onBack) {
                            Text("Back", color = Color.White)
                        }
                        
                        TextButton(
                            onClick = {
                                selectedClassIndex = pagerState.currentPage
                                isEditMode = true
                            },
                            enabled = pagerState.currentPage >= 0
                        ) {
                            Text("Select", color = Color.White)
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = isEditMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                if (selectedClassIndex >= 0) {
                    CharacterEditScreen(
                        characterClass = characterClasses[selectedClassIndex],
                        initialName = characterName,
                        onNameChange = { characterName = it },
                        onSave = { name, stats ->
                            onSaveCharacter(name, stats)
                            onBack()
                        },
                        onBack = { isEditMode = false }
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterCard(
    characterClass: CharacterClass,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(300)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2E5B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Character Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3F6B))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = characterClass.icon,
                    contentDescription = characterClass.name,
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Character Name
            Text(
                text = characterClass.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Character Description
            Text(
                text = characterClass.description,
                fontSize = 14.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                StatRow("Attack", characterClass.baseStats.attack)
                StatRow("Defence", characterClass.baseStats.defence)
                StatRow("Luck", characterClass.baseStats.luck)
                StatRow("Vitality", characterClass.baseStats.vitality)
            }
        }
    }
}

@Composable
fun StatRow(name: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Row {
            // Show stat bars
            for (i in 1..10) {
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 14.dp)
                        .padding(horizontal = 1.dp)
                        .background(
                            color = if (i <= value / 2) Color.White else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterEditScreen(
    characterClass: CharacterClass,
    initialName: String,
    onNameChange: (String) -> Unit,
    onSave: (String, CharacterStats) -> Unit,
    onBack: () -> Unit
) {
    var characterName by remember { mutableStateOf(initialName.ifEmpty { "${characterClass.name} Hero" }) }
    var attack by remember { mutableIntStateOf(characterClass.baseStats.attack) }
    var defence by remember { mutableIntStateOf(characterClass.baseStats.defence) }
    var luck by remember { mutableIntStateOf(characterClass.baseStats.luck) }
    var vitality by remember { mutableIntStateOf(characterClass.baseStats.vitality) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "Customize Your ${characterClass.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.width(48.dp)) // Balance the row
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Character Image
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3F6B))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = characterClass.icon,
                contentDescription = characterClass.name,
                tint = Color.White,
                modifier = Modifier.size(84.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Character Name
        OutlinedTextField(
            value = characterName,
            onValueChange = { 
                characterName = it
                onNameChange(it)
            },
            label = { Text("Character Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Stats sliders
        Text(
            text = "Customize Stats",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        
        StatSlider(
            label = "Attack",
            value = attack,
            onValueChange = { attack = it },
            baseValue = characterClass.baseStats.attack
        )
        
        StatSlider(
            label = "Defence",
            value = defence,
            onValueChange = { defence = it },
            baseValue = characterClass.baseStats.defence
        )
        
        StatSlider(
            label = "Luck",
            value = luck,
            onValueChange = { luck = it },
            baseValue = characterClass.baseStats.luck
        )
        
        StatSlider(
            label = "Vitality",
            value = vitality,
            onValueChange = { vitality = it },
            baseValue = characterClass.baseStats.vitality
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Save Button
        Button(
            onClick = {
                onSave(
                    characterName,
                    CharacterStats(
                        attack = attack,
                        defence = defence,
                        luck = luck,
                        vitality = vitality
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2E5B))
        ) {
            Text("Save Character", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    baseValue: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp
            )
            
            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 5f..25f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF4A90E2),
                inactiveTrackColor = Color.Gray
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Text(
            text = "Base Value: $baseValue",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.End)
        )
    }
}