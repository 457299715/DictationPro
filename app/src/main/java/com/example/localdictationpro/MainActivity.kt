package com.example.localdictationpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localdictationpro.ui.screens.*
import com.example.localdictationpro.ui.theme.LocalDictationProTheme
import com.example.localdictationpro.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            LocalDictationProTheme(settings = settings) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "dictation"
                    ) {
                        composable("dictation") {
                            DictationScreen(
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToBookSelection = { navController.navigate("books") },
                                onNavigateToWordSelection = { navController.navigate("word_selection") },
                                onNavigateToResourceManager = { navController.navigate("resource_manager") }
                            )

                        }

                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("books") {
                            BookSelectionScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToOnlineStore = { navController.navigate("online_store") }
                            )
                        }

                        composable("word_selection") {
                            WordSelectionScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("resource_manager") {
                            ResourceManagerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        // 在 NavHost 中添加
                        composable("online_store") {
                            OnlineStoreScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}