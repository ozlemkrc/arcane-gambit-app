package com.example.arcane_gambit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arcane_gambit.ui.screens.HomeScreen
import com.example.arcane_gambit.ui.screens.LoginScreen
import com.example.arcane_gambit.ui.screens.RegisterScreen
import com.example.arcane_gambit.ui.screens.DashboardScreen
import com.example.arcane_gambit.ui.screens.CharacterManagementScreen
import com.example.arcane_gambit.ui.screens.CreateCharacterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArcaneGambitApp()
        }
    }
}

@Composable
fun ArcaneGambitApp() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("dashboard") },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onLoginClick = { navController.navigate("login") }
        )
    }
        composable("dashboard") {
            DashboardScreen(
                username = "ArcaneHero", // placeholder for now
                onCharacterClick = { navController.navigate("character_management") },
                onArModeClick = { navController.navigate("ar_mode") },
                onLogoutClick = { navController.navigate("home") }
            )
        }

        composable("character_management") {
            CharacterManagementScreen(
                onCreateCharacterClick = { navController.navigate("create_character") },
                onCharacterClick = { characterId -> navController.navigate("character_detail/$characterId") }
            )
        }

        composable("create_character") {
            CreateCharacterScreen(
                onSaveCharacter = { name, level ->
                    // For now, just log the character info
                    println("Character Saved: Name = $name, Level = $level")
                },
                onBack = { navController.popBackStack() }
            )
        }



    }
}
