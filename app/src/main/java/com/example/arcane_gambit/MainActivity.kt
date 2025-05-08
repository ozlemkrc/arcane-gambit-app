package com.example.arcane_gambit

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session manager
        sessionManager = SessionManager(this)

        setContent {
            Arcane_gambitTheme {
                ArcaneGambitApp(sessionManager = sessionManager)
            }
        }
    }

    // Handle new NFC intents
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Store the new intent (important for NFC handling)
        setIntent(intent)

        // Forward the intent to the active screen that needs it
        val nfcHandler = window.decorView.getTag(android.R.id.content) as? ((Intent) -> Unit)
        nfcHandler?.invoke(intent)
    }

    // Handle NFC when activity resumes
    override fun onResume() {
        super.onResume()

        // If this activity was started by an NFC intent, process it
        if (intent != null && intent.action != null &&
            (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
                    intent.action == NfcAdapter.ACTION_TAG_DISCOVERED)) {

            // Forward the intent to current screen
            onNewIntent(intent)
        }
    }
}
@Composable
fun ArcaneGambitApp(sessionManager: SessionManager) {
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
                onBackClick = { navController.popBackStack() }
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