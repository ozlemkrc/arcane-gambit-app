package com.example.arcane_gambit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
}

@Composable
fun ArcaneGambitApp(sessionManager: SessionManager) {
    val navController = rememberNavController()

    // Calculate start destination based on login state
    val startDestination = if (sessionManager.isLoggedIn()) "dashboard" else "home"
    
    // Dummy character data
    val dummyCharacters = listOf(
        Character(id = "1", name = "Warrior", level = 5),
        Character(id = "2", name = "Mage", level = 3),
        Character(id = "3", name = "Rogue", level = 4)
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") },
                onSpectateClick = { navController.navigate("spectate") }
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
            SpectatorScreen()
        }

        composable("dashboard") {
            DashboardScreen(
                username = sessionManager.getUsername() ?: "Player",
                characters = dummyCharacters,
                onCreateCharacterClick = { navController.navigate("create_character") },
                onCharacterClick = { characterId -> 
                    navController.navigate("character_detail/$characterId") 
                },
                onSettingsClick = {
                   navController.navigate("account_settings")
                },
                onLogoutClick = {
                    // Log out and clear session
                    sessionManager.logout()

                    // Navigate to home
                    navController.navigate("home") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("create_character") {
            CreateCharacterScreen(
                onSaveCharacter = { name, level ->
                    // In a real app, save character via API
                    Toast.makeText(
                        navController.context,
                        "Character Saved: $name (Level $level)",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "character_detail/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val characterId = backStackEntry.arguments?.getString("characterId") ?: ""
            val character = dummyCharacters.find { it.id == characterId }

            if (character != null) {
                CharacterPageScreen(
                    character = character,
                    onBackClick = { navController.popBackStack() }
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

        composable("account_settings") {
            AccountSettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    // Log out and clear session
                    sessionManager.logout()

                    // Navigate to home
                    navController.navigate("home") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}