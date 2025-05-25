package com.example.arcane_gambit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.arcane_gambit.ui.screens.Character
import com.example.arcane_gambit.viewmodel.CharacterViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController = rememberNavController(),
    username: String = "Player",
    characterViewModel: CharacterViewModel,
    token: String,
    onCreateCharacterClick: () -> Unit,
    onCharacterClick: (characterId: String) -> Unit,
    onSettingsClick: () -> Unit = { navController.navigate("account_settings") },
    onLogoutClick: () -> Unit,
    onSpectateClick: () -> Unit = { navController.navigate("spectate") }
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Collect ViewModel states
    val characters by characterViewModel.characters.collectAsStateWithLifecycle()
    val isLoading by characterViewModel.isLoading.collectAsStateWithLifecycle()
    val error by characterViewModel.error.collectAsStateWithLifecycle()
    
    // Load characters when the composable is first launched
    LaunchedEffect(token) {
        characterViewModel.loadCharacters(token)
    }
    
    // Handle errors
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error snackbar or handle error
            characterViewModel.clearError()
        }
    }
    
    // Selection mode state
    var selectionMode by remember { mutableStateOf(false) }
    var selectedCharacterIds by remember { mutableStateOf(setOf<String>()) }
    
    // Function to exit selection mode
    fun exitSelectionMode() {
        selectionMode = false
        selectedCharacterIds = emptySet()
    }
    
    // Handle back button press
    BackHandler(enabled = selectionMode) {
        exitSelectionMode()
    }
    
    val drawerItems = listOf(
        DrawerItem(
            title = "Account Settings",
            icon = Icons.Outlined.Settings,
            onClick = onSettingsClick
        ),
        DrawerItem(
            title = "Spectate Mode",
            icon = Icons.Outlined.Visibility,
            onClick = onSpectateClick
        ),
        DrawerItem(
            title = "Logout",
            icon = Icons.Outlined.Logout,
            onClick = onLogoutClick
        )
    )

    // Function to handle deletion of selected characters
    fun deleteSelectedCharacters() {
        if (selectedCharacterIds.isNotEmpty()) {
            characterViewModel.deleteCharacters(token, selectedCharacterIds.toList())
            selectedCharacterIds = emptySet()
            selectionMode = false
        }
    }

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
            // Main content with drawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color(0xFF2A2E5B),
                        drawerContentColor = Color.White,
                        modifier = Modifier.width(300.dp)
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // User profile in drawer header
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6246EA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = username.take(1).uppercase(),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = username,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Text(
                                text = "${characters.size} Characters",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        
                        Divider(
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        // Menu items
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        drawerItems.forEach { item ->
                            NavigationDrawerItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = false,
                                onClick = {
                                    scope.launch {
                                        drawerState.close()
                                        item.onClick()
                                    }
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedIconColor = Color.White,
                                    unselectedTextColor = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            ) {
                // Main content
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = if (selectionMode) "${selectedCharacterIds.size} Selected" else "Arcane Gambit",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent // Keep the same color in both modes
                            ),
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        if (selectionMode) {
                                            exitSelectionMode()
                                        } else {
                                            scope.launch { drawerState.open() }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectionMode) Icons.Default.ArrowBack else Icons.Default.Menu,
                                        contentDescription = if (selectionMode) "Cancel Selection" else "Menu",
                                        tint = Color.White
                                    )
                                }
                            },
                            actions = {
                                AnimatedVisibility(
                                    visible = selectionMode && selectedCharacterIds.isNotEmpty(),
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    IconButton(onClick = { deleteSelectedCharacters() }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Selected",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = !selectionMode,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            FloatingActionButton(
                                onClick = onCreateCharacterClick,
                                containerColor = Color(0xFF6246EA)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Character",
                                    tint = Color.White
                                )
                            }
                        }
                    },                    content = { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Always show the welcome message but with alpha control
                            Text(
                                text = "Welcome",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .alpha(if (selectionMode) 0.0f else 1.0f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Your Characters",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                
                                AnimatedVisibility(
                                    visible = !selectionMode && characters.isNotEmpty() && !isLoading
                                ) {
                                    IconButton(
                                        onClick = { selectionMode = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Select Characters",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            // Show loading indicator
                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFF6246EA),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Loading characters...",
                                            color = Color.White.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            // Show error message
                            else if (error != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF6B2737)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Error",
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = "Failed to load characters",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Text(
                                            text = error ?: "Unknown error",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Button(
                                            onClick = { characterViewModel.loadCharacters(token) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF6246EA)
                                            )
                                        ) {
                                            Text("Retry", color = Color.White)
                                        }
                                    }
                                }
                            }
                            // Show empty state or character list
                            else if (characters.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "No Characters",
                                            tint = Color.White.copy(alpha = 0.4f),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Text(
                                            text = "No characters yet",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Create one to get started!",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(characters) { character ->
                                        val isSelected = selectedCharacterIds.contains(character.id)
                                        
                                        CharacterCard(
                                            character = character,
                                            isSelected = isSelected,
                                            selectionMode = selectionMode,
                                            onClick = { 
                                                if (selectionMode) {
                                                    selectedCharacterIds = if (isSelected) {
                                                        selectedCharacterIds - character.id
                                                    } else {
                                                        selectedCharacterIds + character.id
                                                    }
                                                } else {
                                                    onCharacterClick(character.id)
                                                }
                                            },
                                            onLongClick = {
                                                if (!selectionMode) {
                                                    selectionMode = true
                                                    selectedCharacterIds = setOf(character.id)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    containerColor = Color.Transparent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CharacterCard(
    character: Character,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val elevation = animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        label = "CardElevation"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF4A3D96) // Darker purple for selected cards
            else 
                Color(0xFF2A2E5B) // Original card color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.value)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // Align content to the center vertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White.copy(alpha = 0.7f),
                            checkmarkColor = Color(0xFF6246EA)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Add the character's class type under their name
                Column(
                    verticalArrangement = Arrangement.Center // Center the text vertically
                ) {
                    Text(
                        text = character.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = character.classType, // Display the class type
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            if (!selectionMode) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Character",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}