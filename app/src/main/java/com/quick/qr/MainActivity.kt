package com.quick.qr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.quick.qr.presentation.home.HomeScreen
import com.quick.qr.presentation.saved.SavedQRCodesScreen
import com.quick.qr.presentation.scanner.QRScannerScreen
import com.quick.qr.ui.Screens
import com.quick.qr.ui.theme.QuickQRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickQRTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    LocalContext.current
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    println("currentRoute $currentRoute")
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            Screens.Home::class.qualifiedName -> "Generate QR Code"
                            "com.quick.qr.ui.Screens.QRScanner?encoded={encoded}"-> "Scanner"
                            Screens.SavedQRCodes::class.qualifiedName -> "Saved QR Codes"
                            else -> ""
                        }
                    )
                },
                navigationIcon = {
                    if (currentRoute == "com.quick.qr.ui.Screens.QRScanner?encoded={encoded}" || currentRoute == Screens.SavedQRCodes::class.qualifiedName) {
                        IconButton(onClick = {
                            navController.navigateUp()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (currentRoute == Screens.Home::class.qualifiedName) {
                        IconButton(onClick = {
                            navController.navigate(Screens.SavedQRCodes)
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_collections_bookmark_24),
                                contentDescription = "Open Scanner"
                            )
                        }
                        IconButton(onClick = {
                            navController.navigate(Screens.QRScanner(null))
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_qr_code_scanner_24),
                                contentDescription = "Open Scanner"
                            )
                        }
                    }
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                Navigation(navController = navController)
            }
        }
    )
}

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController, startDestination = Screens.Home) {

        composable<Screens.Home> {
            HomeScreen()
        }

        composable<Screens.QRScanner> {
            val args = it.toRoute<Screens.QRScanner>()
            QRScannerScreen(args.encoded)
        }

        composable<Screens.SavedQRCodes> {
            SavedQRCodesScreen()
        }

    }
}


