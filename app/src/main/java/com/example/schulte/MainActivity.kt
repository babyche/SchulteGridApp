package com.example.schulte

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schulte.model.GameMode
import com.example.schulte.ui.GameScreen
import com.example.schulte.ui.HistoryScreen
import com.example.schulte.ui.HomeScreen
import com.example.schulte.ui.theme.SchulteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SchulteTheme {
                SchulteApp()
            }
        }
    }
}

private sealed interface Screen {
    data object Home : Screen
    data class Game(val mode: GameMode) : Screen
    data object History : Screen
}

@Composable
private fun SchulteApp(viewModel: SchulteViewModel = viewModel()) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    BackHandler(enabled = screen !is Screen.Home) {
        screen = Screen.Home
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen",
    ) { current ->
        when (current) {
            is Screen.Home -> HomeScreen(
                bestTime4x4 = viewModel.bestTime(GameMode.FOUR),
                bestTime5x5 = viewModel.bestTime(GameMode.FIVE),
                recordCount = viewModel.loadRecords().size,
                onModeSelected = { mode -> screen = Screen.Game(mode) },
                onOpenHistory = { screen = Screen.History },
            )
            is Screen.Game -> GameScreen(
                mode = current.mode,
                viewModel = viewModel,
                onBack = { screen = Screen.Home },
            )
            is Screen.History -> HistoryScreen(
                viewModel = viewModel,
                onBack = { screen = Screen.Home },
            )
        }
    }
}