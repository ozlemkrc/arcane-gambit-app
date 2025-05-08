package com.example.arcane_gambit

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import com.example.arcane_gambit.utils.NFCUtil
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.arcane_gambit.ui.screens.*
import com.example.arcane_gambit.ui.theme.Arcane_gambitTheme
import com.example.arcane_gambit.utils.SessionManager

import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var sessionManager: SessionManager
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var currentCharacter: Character? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Set up a pending intent for NFC foreground dispatch
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        setContent {
            Arcane_gambitTheme {
                ArcaneGambitApp(
                    sessionManager = sessionManager,
                    onSelectCharacterForNfc = { character ->
                        // Store the current character being used for NFC
                        currentCharacter = character
                    }
                )
            }
        }

        // Check if the app was launched from an NFC tag
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            processNfcIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Enable NFC foreground dispatch if available
        nfcAdapter?.let { adapter ->
            if (adapter.isEnabled) {
                adapter.enableForegroundDispatch(this, pendingIntent, null, null)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Disable NFC foreground dispatch
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {

            processNfcIntent(intent)
        }
    }

    private fun processNfcIntent(intent: Intent) {
        // Read NDEF messages from the NFC tag if available
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                handleNdefMessage(intent)
            }
            NfcAdapter.ACTION_TECH_DISCOVERED, NfcAdapter.ACTION_TAG_DISCOVERED -> {
                // We have a tag, but no NDEF message, so we'll create one from the current character
                val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }

                if (tag != null) {
                    sendCharacterToGameStation(tag)
                }
            }
        }
    }

    private fun handleNdefMessage(intent: Intent) {
        val rawMessages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, Parcelable::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        }

        if (rawMessages != null) {
            val messages = rawMessages.map { it as NdefMessage }
            for (message in messages) {
                // Use NFCUtil to extract character data
                val character = NFCUtil.extractCharacterFromNdefMessage(message)
                if (character != null) {
                    // Handle the received character
                    onNfcDetected(character)
                    return
                }
            }
        }
        Toast.makeText(this, "No valid character data found", Toast.LENGTH_SHORT).show()
    }

    private fun sendCharacterToGameStation(tag: Tag) {
        // Get the current character that we want to send
        val character = currentCharacter
        if (character != null) {
            // Use our NFCUtil to write the character to the tag
            val success = NFCUtil.writeCharacterToTag(tag, character)

            if (success) {
                // Handle successful write
                onNfcDetected(character)
                Toast.makeText(this, "Character data sent via NFC", Toast.LENGTH_SHORT).show()
            } else {
                // Handle failure
                Toast.makeText(this, "Failed to write character data to NFC tag", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No character selected for NFC", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onNfcDetected(character: Character) {
        // Handle the character joining the game
        Toast.makeText(this, "Character ${character.name} is ready to join the game!", Toast.LENGTH_SHORT).show()

        // In a real app, you would now communicate with the game engine
        // or start a game activity with this character
    }
}

@Composable
fun ArcaneGambitApp(
    sessionManager: SessionManager,
    onSelectCharacterForNfc: (Character) -> Unit
) {
    val navController = rememberNavController()

    // Calculate start destination based on login state
    val startDestination = if (sessionManager.isLoggedIn()) "dashboard" else "home"

    // Store characters in a mutable state list so we can add to it
    var characters by remember { mutableStateOf(listOf(
        Character(id = "1", name = "Warrior", level = 5, strength = 10, agility = 4),
        Character(id = "2", name = "Mage", level = 3, strength = 8, agility = 6),
        Character(id = "3", name = "Archer", level = 4, strength = 3, agility = 5)
    )) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to dashboard and clear backstack
                    navController.navigate("dashboard") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onLoginClick = { navController.navigate("login") }
            )
        }

        composable("spectate") {
            SpectatorScreen(
                navController = navController
            )
        }

        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                username = sessionManager.getUsername() ?: "Player",
                characters = characters,
                onCreateCharacterClick = { navController.navigate("create_character") },
                onCharacterClick = { characterId ->
                    navController.navigate("character_detail/$characterId")
                },
                onSettingsClick = {
                    navController.navigate("account_settings")
                },
                onSpectateClick = {
                    navController.navigate("spectate")
                },
                onLogoutClick = {
                    // Log out and clear session
                    sessionManager.logout()

                    // Navigate to home
                    navController.navigate("home") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onCharactersDelete = { characterIds ->
                    // Remove characters with the given IDs from the list
                    characters = characters.filterNot { it.id in characterIds }
                }
            )
        }

        composable("create_character") {
            CharacterSelectionScreen(
                onSaveCharacter = { name, stats ->
                    // Create a new character from the stats and add to our list
                    val newCharacter = Character(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        level = 1,
                        strength = stats.attack / 2, // Convert attack to strength
                        agility = stats.luck / 3  // Convert luck to agility
                    )

                    // Add the new character to our list
                    characters = characters + newCharacter

                    // Show a success toast
                    Toast.makeText(
                        navController.context,
                        "Character Created: $name",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate back to dashboard explicitly instead of using popBackStack
                    navController.navigate("dashboard") {
                        // Clear the back stack up to dashboard
                        popUpTo("dashboard") { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "character_detail/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId") ?: ""
            val character = characters.find { it.id == characterId }

            if (character != null) {
                CharacterPageScreen(
                    character = character,
                    onBackClick = { navController.popBackStack() },
                    onJoinGameClick = { selectedCharacter ->
                        // Set the character for NFC transmission
                        onSelectCharacterForNfc(selectedCharacter)

                        // Navigate to the placeholder game screen with the character ID
                        navController.navigate("game_placeholder/${selectedCharacter.id}")
                    }
                )
            } else {
                // Handle the case where the character is not found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Character not found",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }

        composable(
            route = "character_page/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId") ?: ""
            val character = characters.find { it.id == characterId } ?: characters[0]

            CharacterPageScreen(
                character = character,
                onBackClick = { navController.popBackStack() },
                onJoinGameClick = { selectedCharacter ->
                    // Set the character for NFC transmission
                    onSelectCharacterForNfc(selectedCharacter)

                    // Navigate to the placeholder game screen with the character ID
                    navController.navigate("game_placeholder/${selectedCharacter.id}")
                }
            )
        }

        composable(
            route = "game_placeholder/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId") ?: ""
            val character = characters.find { it.id == characterId } ?: characters[0]

            GamePlaceholderScreen(
                character = character,
                onBackClick = { navController.popBackStack() },
                onNfcDetected = { detectedCharacter ->
                    // Handle the NFC detected callback
                    Toast.makeText(
                        navController.context,
                        "NFC detected: ${detectedCharacter.name}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // In a real app, this would be where you'd start the game or
                    // send the character data to the game engine
                }
            )
        }

        composable("account_settings") {
            AccountSettingsScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onSpectateClick = {
                    navController.navigate("spectate")
                },
                onLogoutClick = {
                    // Log out and clear session
                    sessionManager.logout()

                    // Navigate to home
                    navController.navigate("home") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                username = sessionManager.getUsername() ?: "Player"
            )
        }
    }
}