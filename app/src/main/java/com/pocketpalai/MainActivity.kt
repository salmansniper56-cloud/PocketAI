package com.pocketpalai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pocketpalai.ui.components.NavDestination
import com.pocketpalai.ui.components.PocketPalDrawerContent
import com.pocketpalai.ui.screens.*
import com.pocketpalai.ui.theme.PocketPalTheme
import com.pocketpalai.ui.viewmodel.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            PocketPalTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PocketPalMainApp(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun PocketPalMainApp(settingsViewModel: SettingsViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentDestination by remember { mutableStateOf(NavDestination.CHAT) }

    val chatViewModel: ChatViewModel = viewModel()
    val modelsViewModel: ModelsViewModel = viewModel()
    val benchmarkViewModel: BenchmarkViewModel = viewModel()

    val sessions by chatViewModel.sessions.collectAsState()
    val activeSessionId by chatViewModel.selectedSessionId.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PocketPalDrawerContent(
                currentDestination = currentDestination,
                sessions = sessions,
                activeSessionId = activeSessionId,
                onSelectDestination = { dest ->
                    currentDestination = dest
                    scope.launch { drawerState.close() }
                },
                onSelectSession = { sessionId ->
                    chatViewModel.selectSession(sessionId)
                    currentDestination = NavDestination.CHAT
                    scope.launch { drawerState.close() }
                },
                onNewChat = {
                    chatViewModel.startNewChat()
                    currentDestination = NavDestination.CHAT
                    scope.launch { drawerState.close() }
                },
                onDeleteSession = { sessionId ->
                    chatViewModel.deleteSession(sessionId)
                }
            )
        }
    ) {
        when (currentDestination) {
            NavDestination.CHAT -> ChatScreen(
                viewModel = chatViewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            NavDestination.MODELS -> ModelsScreen(
                viewModel = modelsViewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            NavDestination.TTS_STUDIO -> TtsStudioScreen(
                modelsViewModel = modelsViewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            NavDestination.BENCHMARK -> BenchmarkScreen(
                viewModel = benchmarkViewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            NavDestination.SETTINGS -> SettingsScreen(
                viewModel = settingsViewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            NavDestination.ABOUT -> AboutScreen(
                viewModel = settingsViewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
    }
}
