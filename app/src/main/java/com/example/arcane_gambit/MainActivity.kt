package com.example.arcane_gambit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.arcane_gambit.nfc.NfcSimulator
import com.example.arcane_gambit.ui.screens.*
import com.example.arcane_gambit.ui.theme.Arcane_gambitTheme
import com.example.arcane_gambit.utils.SessionManager
import com.example.arcane_gambit.viewmodels.NfcViewModel

class MainActivity : ComponentActivity() {
    private val nfcViewModel: NfcViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize session manager
        sessionManager = SessionManager(this)
        nfcViewModel.initializeNfcManager(this)

        setContent {
            // Observe auto-login success
            val autoLoginSuccess by nfcViewModel.autoLoginSuccess.observeAsState(false)

            // React to auto-login
            LaunchedEffect(autoLoginSuccess) {
                if (autoLoginSuccess) {
                    Toast.makeText(
                        this@MainActivity,
                        "Logged in with RFID tag",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            Arcane_gambitTheme {
                ArcaneGambitApp(
                    nfcViewModel = nfcViewModel,
                    sessionManager = sessionManager,
                    onSimulateNfcScan = { simulateNfcScan() }
                )
            }
        }

        // Process intent in case app was started via NFC
        processIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Enable NFC foreground dispatch
        nfcViewModel.enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        // Disable NFC foreground dispatch
        nfcViewModel.disableNfcForegroundDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        // Process NFC intent
        nfcViewModel.processNfcIntent(intent)
    }


    // Add this method to MainActivity to expose the NfcViewModel to the NfcSimulator
    @Suppress("unused")  // This suppresses the "unused" warning
    fun providNfcViewModel(): NfcViewModel {
        return nfcViewModel
    }



    /**
     * Simulate an NFC tag scan for testing
     */
    private fun simulateNfcScan() {
        // Generate a random tag ID for testing
        val tagId = "04A5B9C2" + System.currentTimeMillis().toString().takeLast(4)

        // Use NfcSimulator to simulate a tag scan
        NfcSimulator.simulateNfcScan(this, tagId)
    }
}

@Composable
fun ArcaneGambitApp(
    nfcViewModel: NfcViewModel,
    sessionManager: SessionManager,
    onSimulateNfcScan: () -> Unit
) {
    val navController = rememberNavController()

    // Observe NFC state
    val isNfcAvailable by nfcViewModel.isNfcAvailable.observeAsState(false)
    val isNfcEnabled by nfcViewModel.isNfcEnabled.observeAsState(false)
    val currentRfidTag by nfcViewModel.currentRfidTag.observeAsState(null)
    val autoLoginSuccess by nfcViewModel.autoLoginSuccess.observeAsState(false)

    // Calculate start destination based on login state
    val startDestination = if (sessionManager.isLoggedIn() || autoLoginSuccess) "dashboard" else "home"

    // Effect to handle automatic navigation on auto-login
    LaunchedEffect(autoLoginSuccess) {
        if (autoLoginSuccess && navController.currentDestination?.route != "dashboard") {
            navController.navigate("dashboard") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Scaffold to hold the navigation and the test button
    Scaffold(
        floatingActionButton = {
            if (!isNfcAvailable || !isNfcEnabled) {
                FloatingActionButton(
                    onClick = onSimulateNfcScan,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Nfc ,
                        contentDescription = "Simulate NFC Scan"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
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
                        onRegisterClick = { navController.navigate("register") },
                        rfidTag = currentRfidTag,
                        isNfcEnabled = isNfcEnabled
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onLoginClick = { navController.navigate("login") },
                        rfidTag = currentRfidTag,
                        isNfcEnabled = isNfcEnabled
                    )
                }

                composable("dashboard") {
                    DashboardScreen(
                        username = sessionManager.getUsername() ?: "Player",
                        onCharacterClick = { navController.navigate("character_management") },
                        onArModeClick = { /* AR mode not implemented yet */ },
                        onLogoutClick = {
                            // Log out and clear session
                            sessionManager.logout()

                            // Clear RFID tag
                            nfcViewModel.clearRfidTag()

                            // Navigate to home
                            navController.navigate("home") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    )
                }

                composable("character_management") {
                    CharacterManagementScreen(
                        onCreateCharacterClick = { navController.navigate("create_character") },
                        onCharacterClick = { characterId ->
                            navController.navigate("character_detail/$characterId")
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
                    // For now just show character name
                    Text("Character Details for: $characterId")
                }
            }

            // NFC Simulator Information
            if (!isNfcAvailable || !isNfcEnabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    Card(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "NFC ${if (!isNfcAvailable) "not available" else "disabled"}. " +
                                    "Using simulator - tap FAB to simulate a scan.",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}