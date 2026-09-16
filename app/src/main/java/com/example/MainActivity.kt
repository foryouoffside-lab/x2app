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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DrillMode
import com.example.model.DrillType
import com.example.ui.components.BottomNavBar
import com.example.ui.components.CalibrationSheet
import com.example.ui.components.CoachExportDialog
import com.example.ui.components.CoachInsightSheet
import com.example.ui.components.DrillDetailSheet
import com.example.ui.components.GlobalHeader
import com.example.ui.components.MetricDetailSheet
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.ScoringWorksSheet
import com.example.ui.components.SportsBatterySheet
import com.example.ui.screens.ActiveDrillScreen
import com.example.ui.screens.CompeteScreen
import com.example.ui.screens.buildPerformanceMetrics
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
    // Trends, benchmarks and records are built only from measured Test runs; Train runs
    // ramp difficulty mid-run, so their times are not comparable between sessions.
    val testSessions = remember(sessions) { sessions.filter { it.mode == DrillMode.TEST.name } }
    val notifications by viewModel.notifications.collectAsState()
    val hasUnread = notifications.any { it.isUnread }

    val activeDrillType by viewModel.activeDrillType.collectAsState()
    val isDailyMode by viewModel.isDailyMode.collectAsState()
    val activeDrillMode by viewModel.activeDrillMode.collectAsState()
    val displayLatencyMs by viewModel.displayLatencyMs.collectAsState()
    val touchSamplingOffsetMs by viewModel.touchSamplingOffsetMs.collectAsState()
    val panelLatencyMs by viewModel.panelLatencyMs.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()

    val selectedDrillForSheet by viewModel.selectedDrillForSheet.collectAsState()
    val showCoachInsight by viewModel.showCoachInsightSheet.collectAsState()
    val coachInsightText by viewModel.coachInsightContent.collectAsState()
    val isLoadingCoachInsight by viewModel.isLoadingCoachInsight.collectAsState()
    val showNotifications by viewModel.showNotificationsSheet.collectAsState()
    val selectedMetricDetail by viewModel.selectedMetricDetail.collectAsState()
    val showCalibration by viewModel.showCalibrationFlow.collectAsState()
    val showScoringWorks by viewModel.showScoringWorksSheet.collectAsState()

    val trainFilter by viewModel.trainFilter.collectAsState()
    val progressRange by viewModel.progressTimeRange.collectAsState()
    val soundEnabled by viewModel.soundCuesEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()

    val showBattery by viewModel.showBatterySheet.collectAsState()
    val selectedBattery by viewModel.selectedBattery.collectAsState()
    val sportsBatteries = viewModel.sportsBatteries

    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val exportCsvContent by viewModel.exportCsvContent.collectAsState()

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
                mode = activeDrillMode,
                displayLatencyMs = displayLatencyMs,
                onCompleteRun = { median, best, accuracy, consistency, cv, falseStarts, ies, exTau, cnsHz, trials, runMode, survivedSec, levelReached ->
                    viewModel.completeDrillRun(
                        type = activeDrillType,
                        medianMs = median,
                        bestMs = best,
                        accuracyPercent = accuracy,
                        consistencyMs = consistency,
                        cvPercent = cv,
                        falseStarts = falseStarts,
                        iesScore = ies,
                        exGaussianTau = exTau,
                        cnsHz = cnsHz,
                        rawTrials = trials,
                        mode = runMode,
                        survivedSec = survivedSec,
                        levelReached = levelReached
                    )
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
                        avatarInitials = userProfile.name.take(1).ifBlank { "A" }.uppercase(),
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
                            val choiceSpeedMs = viewModel.averageChoiceSpeedMs()
                            val coachHeadline = if (choiceSpeedMs != null && userProfile.fastestMs > 0) {
                                val delta = choiceSpeedMs - userProfile.fastestMs
                                if (delta > 0) "Choice speed trailing visual speed (+$delta ms)" else "Choice speed matching visual speed"
                            } else ""
                            val recommendedDuration = viewModel.coreDrills.find { it.type == DrillType.CHOICE }?.defaultDuration ?: "2 min"
                            HomeScreen(
                                userProfile = userProfile,
                                sessions = testSessions,
                                onStartSession = { type -> viewModel.startDrill(type) },
                                onPlayChallenge = { viewModel.startDrill(DrillType.FLASH_GRID, dailyMode = true) },
                                coachHeadline = coachHeadline,
                                recommendedDuration = recommendedDuration,
                                onOpenWhyCoach = { viewModel.openCoachInsight() },
                                onNavigateToProgress = { viewModel.selectTab(AppTab.PROGRESS) }
                            )
                        }

                        AppTab.TRAIN -> {
                            TrainScreen(
                                drills = viewModel.coreDrills,
                                selectedFilter = trainFilter,
                                onFilterSelect = { filter -> viewModel.setTrainFilter(filter) },
                                onDrillClick = { drill -> viewModel.openDrillDetail(drill) },
                                onStartRecommended = { viewModel.startDrill(DrillType.CHOICE) },
                                onContinueTraining = { viewModel.startDrill(DrillType.CLASSIC) },
                                onYourPlanClick = { viewModel.openCoachInsight() },
                                batteries = sportsBatteries,
                                onOpenBattery = { battery -> viewModel.openBatteryDetail(battery) },
                                sessions = sessions
                            )
                        }

                        AppTab.COMPETE -> {
                            CompeteScreen(
                                sessions = testSessions,
                                onPlayToday = { viewModel.startDrill(DrillType.FLASH_GRID, dailyMode = true) },
                                onSeeRecords = { viewModel.selectTab(AppTab.PROGRESS) }
                            )
                        }

                        AppTab.PROGRESS -> {
                            ProgressScreen(
                                userProfile = userProfile,
                                sessions = testSessions,
                                selectedRange = progressRange,
                                onRangeSelect = { range -> viewModel.setProgressTimeRange(range) },
                                onMetricClick = { metric -> viewModel.openMetricDetail(metric) },
                                onBuildSession = { viewModel.startDrill(DrillType.CHOICE) },
                                onViewAllRecords = {
                                    val speedMetric = buildPerformanceMetrics(sessions).first { it.name == "Speed" }
                                    viewModel.openMetricDetail(speedMetric)
                                },
                                onExportCsv = { viewModel.openExportDialog() },
                                onDeleteSession = { id -> viewModel.deleteSession(id) }
                            )
                        }

                        AppTab.PROFILE -> {
                            ProfileScreen(
                                userProfile = userProfile,
                                onOpenCalibration = { viewModel.openCalibration() },
                                onOpenNotifications = { viewModel.openNotifications() },
                                onOpenScoringWorks = { viewModel.openScoringWorks() },
                                soundEnabled = soundEnabled,
                                onSoundToggle = { viewModel.setSoundCuesEnabled(it) },
                                hapticsEnabled = hapticsEnabled,
                                onHapticsToggle = { viewModel.setHapticsEnabled(it) }
                            )
                        }
                    }
                }
            }

            // Bottom Sheets
            selectedDrillForSheet?.let { drill ->
                DrillDetailSheet(
                    drill = drill,
                    soundEnabled = soundEnabled,
                    onSoundToggle = { viewModel.setSoundCuesEnabled(it) },
                    onStartDrill = { chosenMode -> viewModel.startDrill(drill.type, mode = chosenMode) },
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

            selectedMetricDetail?.let { metric ->
                MetricDetailSheet(
                    metric = metric,
                    onDismiss = { viewModel.closeMetricDetail() }
                )
            }

            if (showCalibration) {
                CalibrationSheet(
                    touchSamplingOffsetMs = touchSamplingOffsetMs,
                    panelLatencyMs = panelLatencyMs,
                    onSaveCalibration = { touchMs, panelMs, hz -> viewModel.saveCalibration(touchMs, panelMs, hz) },
                    onDismiss = { viewModel.closeCalibration() }
                )
            }

            if (showScoringWorks) {
                ScoringWorksSheet(
                    onDismiss = { viewModel.closeScoringWorks() }
                )
            }

            selectedBattery?.let { battery ->
                SportsBatterySheet(
                    battery = battery,
                    onStartBattery = { b -> viewModel.startBatteryFirstDrill(b) },
                    onDismiss = { viewModel.closeBatteryDetail() }
                )
            }

            if (showExportDialog) {
                CoachExportDialog(
                    csvContent = exportCsvContent,
                    onDismiss = { viewModel.closeExportDialog() }
                )
            }
        }
    }
}
