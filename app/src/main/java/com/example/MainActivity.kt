package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.AircraftDarkBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WarOfAircraftApp()
            }
        }
    }
}

@Composable
fun WarOfAircraftApp(viewModel: GameViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AircraftDarkBg)
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                AppScreen.MAIN_MENU -> {
                    MainMenuScreen(
                        viewModel = viewModel,
                        onStartMissionSelect = { viewModel.navigateTo(AppScreen.MISSION_SELECT) },
                        onOpenDynamicOperations = { viewModel.navigateTo(AppScreen.DYNAMIC_OPERATIONS) },
                        onStartSurvival = { viewModel.startSurvival() },
                        onOpenHangar = { viewModel.navigateTo(AppScreen.HANGAR) },
                        onOpenAchievements = { viewModel.navigateTo(AppScreen.ACHIEVEMENTS) }
                    )
                }
                AppScreen.DYNAMIC_OPERATIONS -> {
                    DynamicOperationsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        onStartDynamicMission = { mission -> viewModel.startDynamicMission(mission) }
                    )
                }
                AppScreen.MISSION_SELECT -> {
                    MissionSelectScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        onStartMission = { missionId -> viewModel.startMission(missionId) }
                    )
                }
                AppScreen.HANGAR -> {
                    HangarScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
                    )
                }
                AppScreen.ACHIEVEMENTS -> {
                    AchievementsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
                    )
                }
                AppScreen.GAMEPLAY -> {
                    GamePlayScreen(
                        viewModel = viewModel,
                        onReturnToMenu = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        onNavigateToHangar = { viewModel.navigateTo(AppScreen.HANGAR) }
                    )
                }
            }
        }
    }
}
