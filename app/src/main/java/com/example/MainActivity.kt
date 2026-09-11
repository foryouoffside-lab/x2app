package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DrillType
import com.example.model.PerformanceMetric
import com.example.ui.components.BottomNavBar
import com.example.ui.components.CalibrationSheet
import com.example.ui.components.CoachInsightSheet
import com.example.ui.components.DrillDetailSheet
import com.example.ui.components.GlobalHeader
import com.example.ui.components.MetricDetailSheet
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.PlayerSummarySheet
import com.example.ui.components.ScoringWorksSheet
import com.example.ui.screens.ActiveDrillScreen
import com.example.ui.screens.CompeteScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.TrainScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ReactionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReactionTheme {
                ReactionApp()
            }
        }
    }
}

@Composable
fun ReactionApp(
    viewModel: ReactionViewModel = viewModel()
) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val hasUnread = notifications.any { it.isUnread }

    val activeDrillType by viewModel.activeDrillType.collectAsState()
    val isDailyMode by viewModel.isDailyMode.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()

    val selectedDrillForSheet by viewModel.selectedDrillForSheet.collectAsState()
    val showCoachInsight by viewModel.showCoachInsightSheet.collectAsState()
    val coachInsightText by viewModel.coachInsightContent.collectAsState()
    val isLoadingCoachInsight by viewModel.isLoadingCoachInsight.collectAsState()
    val showNotifications by viewModel.showNotificationsSheet.collectAsState()
    val selectedPlayerSummary by viewModel.selectedPlayerSummary.collectAsState()
    val selectedMetricDetail by viewModel.selectedMetricDetail.collectAsState()
    val showCalibration by viewModel.showCalibrationFlow.collectAsState()
    val showScoringWorks by viewModel.showScoringWorksSheet.collectAsState()

    val trainFilter by viewModel.trainFilter.collectAsState()
    val leaderboardTab by viewModel.leaderboardTab.collectAsState()
    val progressRange by viewModel.progressTimeRange.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()
    val soundEnabled by viewModel.soundCuesEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()

    when (activeScreen) {
        ActiveScreen.ACTIVE_DRILL -> {
            BackHandler {
                viewModel.exitActiveDrill()
            }
            ActiveDrillScreen(
                drillType = activeDrillType,
                isDailyMode = isDailyMode,
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                onExitDrill = { viewModel.exitActiveDrill() },
                onCompleteRun = { median, best, accuracy, consistency ->
                    viewModel.completeDrillRun(activeDrillType, median, best, accuracy, consistency)
                }
            )
        }

        ActiveScreen.RESULT -> {
            BackHandler {
                viewModel.selectTab(AppTab.HOME)
            }
            lastResult?.let { result ->
                ResultScreen(
                    result = result,
                    onTrainAgain = { viewModel.startDrill(result.drillType, isDailyMode) },
                    onViewInProgress = { viewModel.selectTab(AppTab.PROGRESS) },
                    onClose = { viewModel.selectTab(AppTab.HOME) }
                )
            } ?: run {
                viewModel.selectTab(AppTab.HOME)
            }
        }

        ActiveScreen.TABS -> {
            Scaffold(
                topBar = {
                    GlobalHeader(
                        hasUnreadNotifications = hasUnread,
                        onWordmarkClick = { viewModel.selectTab(AppTab.HOME) },
                        onNotificationsClick = { viewModel.openNotifications() },
                        onAvatarClick = { viewModel.selectTab(AppTab.PROFILE) }
                    )
                },
                bottomBar = {
                    BottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = { tab -> viewModel.selectTab(tab) }
                    )
                },
                containerColor = DarkBackground
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(DarkBackground)
                ) {
                    when (currentTab) {
                        AppTab.HOME -> {
                            HomeScreen(
                                userProfile = userProfile,
                                onStartSession = { type -> viewModel.startDrill(type) },
                                onPlayChallenge = { viewModel.startDrill(DrillType.FLASH_GRID, dailyMode = true) },
                                onOpenWhyCoach = { viewModel.openCoachInsight() },
                                onNavigateToProgress = { viewModel.selectTab(AppTab.PROGRESS) }
                            )
                        }

                        AppTab.TRAIN -> {
                            TrainScreen(
                                drills = viewModel.coreDrills,
                                selectedFilter = trainFilter,
                                onFilterSelect = { filter -> viewModel.trainFilter.value = filter },
                                onDrillClick = { drill -> viewModel.openDrillDetail(drill) },
                                onStartRecommended = { viewModel.startDrill(DrillType.CHOICE) },
                                onContinueTraining = { viewModel.startDrill(DrillType.CLASSIC) },
                                onYourPlanClick = { viewModel.openCoachInsight() }
                            )
                        }

                        AppTab.COMPETE -> {
                            val players = when (leaderboardTab) {
                                "Country" -> viewModel.countryPlayers
                                "Friends" -> viewModel.friendPlayers
                                else -> viewModel.globalPlayers
                            }
                            CompeteScreen(
                                currentTab = leaderboardTab,
                                onTabSelected = { tab -> viewModel.leaderboardTab.value = tab },
                                players = players,
                                onPlayToday = { viewModel.startDrill(DrillType.FLASH_GRID, dailyMode = true) },
                                onPlayerClick = { player -> viewModel.openPlayerSummary(player) },
                                onSeeRecords = { viewModel.selectTab(AppTab.PROGRESS) }
                            )
                        }

                        AppTab.PROGRESS -> {
                            ProgressScreen(
                                userProfile = userProfile,
                                sessions = sessions,
                                selectedRange = progressRange,
                                onRangeSelect = { range -> viewModel.progressTimeRange.value = range },
                                onMetricClick = { metric -> viewModel.openMetricDetail(metric) },
                                onBuildSession = { viewModel.startDrill(DrillType.CHOICE) },
                                onViewAllRecords = { viewModel.openMetricDetail(PerformanceMetric("Speed", 82, "Median 236 ms", "+3.4% this month", "Simple visual reflex latency")) }
                            )
                        }

                        AppTab.PROFILE -> {
                            ProfileScreen(
                                userProfile = userProfile,
                                onOpenCalibration = { viewModel.openCalibration() },
                                onOpenNotifications = { viewModel.openNotifications() }
                            )
                        }
                    }
                }
            }

            // Bottom Sheets
            selectedDrillForSheet?.let { drill ->
                DrillDetailSheet(
                    drill = drill,
                    selectedDuration = selectedDuration,
                    onDurationSelected = { viewModel.selectedDuration.value = it },
                    soundEnabled = soundEnabled,
                    onSoundToggle = { viewModel.soundCuesEnabled.value = it },
                    onStartDrill = { viewModel.startDrill(drill.type) },
                    onHowScoringWorks = { viewModel.openScoringWorks() },
                    onDismiss = { viewModel.closeDrillDetail() }
                )
            }

            if (showCoachInsight) {
                CoachInsightSheet(
                    insightText = coachInsightText,
                    isLoading = isLoadingCoachInsight,
                    onDismiss = { viewModel.closeCoachInsight() }
                )
            }

            if (showNotifications) {
                NotificationsSheet(
                    notifications = notifications,
                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                    onDismiss = { viewModel.closeNotifications() }
                )
            }

            selectedPlayerSummary?.let { player ->
                PlayerSummarySheet(
                    player = player,
                    onDismiss = { viewModel.closePlayerSummary() }
                )
            }

            selectedMetricDetail?.let { metric ->
                MetricDetailSheet(
                    metric = metric,
                    onDismiss = { viewModel.closeMetricDetail() }
                )
            }

            if (showCalibration) {
                CalibrationSheet(
                    onDismiss = { viewModel.closeCalibration() }
                )
            }

            if (showScoringWorks) {
                ScoringWorksSheet(
                    onDismiss = { viewModel.closeScoringWorks() }
                )
            }
        }
    }
}
