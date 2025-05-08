package com.example.arcane_gambit.ui.screens

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arcane_gambit.utils.GameEngineConnector
import com.example.arcane_gambit.utils.NFCHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Define NFC states
enum class NfcState {
    CHECKING,     // Checking if NFC is available
    NOT_SUPPORTED,// Device doesn't support NFC
    DISABLED,     // NFC is disabled on the device
    READY,        // NFC is ready, waiting for tag
    TRANSFERRING, // Data is being transferred
    SUCCESS,      // Data transferred successfully
    ERROR         // Error during transfer
}

// Define game connection states
enum class GameConnectionState {
    DISCONNECTED, // Not connected to game
    CONNECTING,   // Trying to connect to game
    CONNECTED,    // Connected to game
    JOINED,       // Successfully joined game session
    ERROR         // Error during connection
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlaceholderScreen(
    character: Character,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    // Initialize helper classes
    val nfcHelper = remember { NFCHelper(activity) }
    val gameEngineConnector = remember { GameEngineConnector() }

    var nfcState by remember { mutableStateOf(NfcState.CHECKING) }
    var gameState by remember { mutableStateOf(GameConnectionState.DISCONNECTED) }
    var gameStatus by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Animation for NFC pulse effect
    val infiniteTransition = rememberInfiniteTransition(label = "nfcPulse")
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Check NFC availability when component is first displayed
    LaunchedEffect(Unit) {
        delay(500) // Short delay for UI to load

        when {
            !nfcHelper.hasNFCSupport() -> {
                nfcState = NfcState.NOT_SUPPORTED
                Toast.makeText(context, "NFC is not supported on this device", Toast.LENGTH_LONG).show()
            }
            !nfcHelper.isNFCEnabled() -> {
                nfcState = NfcState.DISABLED
                Toast.makeText(context, "Please enable NFC in your device settings", Toast.LENGTH_LONG).show()
            }
            else -> {
                nfcState = NfcState.READY
                nfcHelper.enableNFCForegroundDispatch()

                // Connect to game engine
                gameState = GameConnectionState.CONNECTING
                val connected = gameEngineConnector.connect()
                gameState = if (connected) GameConnectionState.CONNECTED else GameConnectionState.ERROR
            }
        }
    }

    // Effect to disable NFC when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            try {
                nfcHelper.disableNFCForegroundDispatch()
                scope.launch {
                    gameEngineConnector.disconnect()
                }
            } catch (e: Exception) {
                Log.e("GameScreen", "Error cleaning up resources: ${e.message}")
            }
        }
    }

    // Fetch game status when we join a game
    LaunchedEffect(gameState) {
        if (gameState == GameConnectionState.JOINED) {
            gameStatus = gameEngineConnector.getGameStatus()
        }
    }

    // Handle NFC intent when the activity is resumed with a new intent
    fun handleNfcIntent(intent: Intent?) {
        if (intent == null || intent.action == null) return

        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {

            scope.launch {
                nfcState = NfcState.TRANSFERRING
                delay(1000) // Simulate processing time

                val tag = intent.getParcelableExtra<android.nfc.Tag>(NfcAdapter.EXTRA_TAG)
                if (tag != null) {
                    val success = nfcHelper.writeCharacterToTag(tag, character)
                    nfcState = if (success) NfcState.SUCCESS else NfcState.ERROR

                    if (success) {
                        Toast.makeText(context, "Character data transferred successfully!", Toast.LENGTH_LONG).show()

                        // Send character data to game engine and join session
                        if (gameState == GameConnectionState.CONNECTED) {
                            val dataSent = gameEngineConnector.sendCharacterData(character)
                            if (dataSent) {
                                val joined = gameEngineConnector.joinGameSession()
                                gameState = if (joined) GameConnectionState.JOINED else GameConnectionState.ERROR

                                if (joined) {
                                    // Get game status
                                    gameStatus = gameEngineConnector.getGameStatus()
                                }
                            } else {
                                gameState = GameConnectionState.ERROR
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to transfer data to NFC tag", Toast.LENGTH_LONG).show()
                    }
                } else {
                    nfcState = NfcState.ERROR
                    Toast.makeText(context, "No NFC tag detected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Remember the current activity to handle new intents
    LaunchedEffect(context) {
        val mainActivity = context as? Activity
        mainActivity?.intent?.let { intent ->
            handleNfcIntent(intent)
        }
    }

    // Method to be called from MainActivity when a new intent is received
    fun onNewIntent(intent: Intent) {
        handleNfcIntent(intent)
    }

    // Set this function to be accessible from MainActivity
    (context as? Activity)?.let { activity ->
        activity.window.decorView.setTag(android.R.id.content, ::onNewIntent)
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
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Character info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2F50)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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

                        // Status text
                        val statusText = when {
                            gameState == GameConnectionState.JOINED -> "Joined the game!"
                            gameState == GameConnectionState.ERROR -> "Error connecting to game"
                            nfcState == NfcState.ERROR -> "NFC transfer failed"
                            nfcState == NfcState.NOT_SUPPORTED -> "NFC not supported"
                            nfcState == NfcState.DISABLED -> "NFC is disabled"
                            else -> "Ready to join game"
                        }

                        Text(
                            text = statusText,
                            color = when {
                                gameState == GameConnectionState.JOINED -> Color(0xFF4CAF50)
                                gameState == GameConnectionState.ERROR || nfcState == NfcState.ERROR -> Color(0xFFF44336)
                                else -> Color.White.copy(alpha = 0.7f)
                            },
                            fontSize = 14.sp
                        )
                    }
                }

                // Game session info (when joined)
                AnimatedVisibility(
                    visible = gameState == GameConnectionState.JOINED,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1B5E20).copy(alpha = 0.8f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Game Session Active",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Divider(color = Color.White.copy(alpha = 0.2f))

                            // Display game status information from the game engine
                            val displayGameInfo = mapOf(
                                "Game Mode" to (gameStatus["gameMode"] ?: "Standard"),
                                "Players Connected" to "${gameStatus["playersConnected"] ?: "0"}/${gameStatus["maxPlayers"] ?: "6"}",
                                "Map" to (gameStatus["mapName"] ?: "Mystic Forest"),
                                "Time Left" to (gameStatus["timeLeft"] ?: "15 minutes")
                            )

                            displayGameInfo.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = value.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Your character is now in the game!",
                                fontSize = 16.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // NFC Section (only show when not joined game)
                AnimatedVisibility(
                    visible = gameState != GameConnectionState.JOINED,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                nfcState == NfcState.SUCCESS -> Color(0xFF1B5E20).copy(alpha = 0.8f)
                                nfcState == NfcState.ERROR || gameState == GameConnectionState.ERROR ->
                                    Color(0xFFB71C1C).copy(alpha = 0.8f)
                                else -> Color(0xFF2A2F50)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Icon based on state
                            Box(
                                modifier = Modifier.size(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    nfcState == NfcState.CHECKING || gameState == GameConnectionState.CONNECTING -> {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                    nfcState == NfcState.SUCCESS -> {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Success",
                                            tint = Color.White,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                    nfcState == NfcState.ERROR || gameState == GameConnectionState.ERROR -> {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Error",
                                            tint = Color.White,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                    nfcState == NfcState.NOT_SUPPORTED || nfcState == NfcState.DISABLED -> {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Not Available",
                                            tint = Color.White,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                    nfcState == NfcState.READY && gameState == GameConnectionState.CONNECTED -> {
                                        // Pulsing NFC icon for ready state
                                        Icon(
                                            imageVector = Icons.Default.Nfc,
                                            contentDescription = "NFC Icon",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier
                                                .size(64.dp)
                                                .scale(pulseScale.value)
                                        )
                                    }
                                    nfcState == NfcState.TRANSFERRING -> {
                                        Icon(
                                            imageVector = Icons.Default.Nfc,
                                            contentDescription = "NFC Icon",
                                            tint = Color.Yellow,
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Default.Nfc,
                                            contentDescription = "NFC Icon",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                }
                            }

                            // Status header text
                            Text(
                                text = when {
                                    nfcState == NfcState.CHECKING -> "DETECTING NFC..."
                                    nfcState == NfcState.NOT_SUPPORTED -> "NFC NOT SUPPORTED"
                                    nfcState == NfcState.DISABLED -> "NFC IS DISABLED"
                                    nfcState == NfcState.READY && gameState == GameConnectionState.CONNECTED ->
                                        "TAP YOUR NFC DEVICE"
                                    nfcState == NfcState.TRANSFERRING -> "TRANSFERRING DATA..."
                                    nfcState == NfcState.SUCCESS -> "TRANSFER COMPLETE"
                                    gameState == GameConnectionState.ERROR -> "GAME CONNECTION ERROR"
                                    nfcState == NfcState.ERROR -> "NFC TRANSFER ERROR"
                                    else -> "READY"
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Status description text
                            Text(
                                text = when {
                                    nfcState == NfcState.CHECKING -> "Checking if your device supports NFC..."
                                    nfcState == NfcState.NOT_SUPPORTED -> "Your device does not support NFC. Please use a different device."
                                    nfcState == NfcState.DISABLED -> "Please enable NFC in your device settings to continue."
                                    nfcState == NfcState.READY && gameState == GameConnectionState.CONNECTED ->
                                        "Tap your NFC device to the game station to join the game with ${character.name}"
                                    nfcState == NfcState.TRANSFERRING -> "Please keep your device still while transferring character data..."
                                    nfcState == NfcState.SUCCESS -> "Character data has been transferred. Joining game..."
                                    gameState == GameConnectionState.ERROR -> "There was an error connecting to the game. Please try again."
                                    nfcState == NfcState.ERROR -> "There was an error transferring your character. Please try again."
                                    else -> "Ready to start the process"
                                },
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Loading indicators
                if (nfcState == NfcState.TRANSFERRING || gameState == GameConnectionState.CONNECTING) {
                    LinearProgressIndicator(
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Action buttons based on state
                when {
                    // Enable NFC button
                    nfcState == NfcState.DISABLED -> {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open NFC settings", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open NFC Settings")
                        }
                    }

                    // Retry button for errors
                    nfcState == NfcState.ERROR || gameState == GameConnectionState.ERROR -> {
                        Button(
                            onClick = {
                                scope.launch {
                                    // Reset states and try again
                                    if (gameState == GameConnectionState.ERROR) {
                                        gameState = GameConnectionState.CONNECTING
                                        val connected = gameEngineConnector.connect()
                                        gameState = if (connected) GameConnectionState.CONNECTED else GameConnectionState.ERROR
                                    }

                                    if (nfcState == NfcState.ERROR) {
                                        nfcState = if (nfcHelper.isNFCEnabled()) NfcState.READY else NfcState.DISABLED
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }

                // Stats summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2F50)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Character Stats",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Divider(color = Color.White.copy(alpha = 0.2f))

                        // Stats in a row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CompactStatItem("ATK", (character.strength * 2).toString(), Color(0xFFF44336))
                            CompactStatItem("DEF", ((character.strength / 2) + (character.agility / 2) + 5).toString(), Color(0xFF2196F3))
                            CompactStatItem("LUCK", (character.agility * 3).toString(), Color(0xFFFFEB3B))
                            CompactStatItem("VIT", (character.level * 5).toString(), Color(0xFF4CAF50))
                        }
                    }
                }

                // Information about NFC at the bottom
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1B1F3B)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How it works",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Text(
                            text = "1. Enable NFC on your device\n" +
                                    "2. Tap your device to the game station\n" +
                                    "3. Your character data will transfer to the game\n" +
                                    "4. The game engine will prepare your character\n" +
                                    "5. You'll be ready to play!",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF1B1F3B)
    )
}

@Composable
fun CompactStatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
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