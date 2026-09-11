package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiCoachService
import com.example.data.ReactionDatabase
import com.example.data.SessionEntity
import com.example.model.DrillInfo
import com.example.model.DrillRunResult
import com.example.model.DrillType
import com.example.model.LeaderboardPlayer
import com.example.model.NotificationItem
import com.example.model.PerformanceMetric
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppTab {
    HOME, TRAIN, COMPETE, PROGRESS, PROFILE
}

enum class ActiveScreen {
    TABS,
    ACTIVE_DRILL,
    RESULT
}

class ReactionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ReactionDatabase.getInstance(application)
    private val dao = database.reactionDao()

    val sessions: StateFlow<List<SessionEntity>> = dao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation state
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.TABS)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    // Active Drill Configuration & Results
    private val _selectedDrillForSheet = MutableStateFlow<DrillInfo?>(null)
    val selectedDrillForSheet: StateFlow<DrillInfo?> = _selectedDrillForSheet.asStateFlow()

    private val _activeDrillType = MutableStateFlow(DrillType.CLASSIC)
    val activeDrillType: StateFlow<DrillType> = _activeDrillType.asStateFlow()

    private val _isDailyMode = MutableStateFlow(false)
    val isDailyMode: StateFlow<Boolean> = _isDailyMode.asStateFlow()

    private val _lastResult = MutableStateFlow<DrillRunResult?>(null)
    val lastResult: StateFlow<DrillRunResult?> = _lastResult.asStateFlow()

    // Modals / Sheets
    private val _showCoachInsightSheet = MutableStateFlow(false)
    val showCoachInsightSheet: StateFlow<Boolean> = _showCoachInsightSheet.asStateFlow()

    private val _coachInsightContent = MutableStateFlow("")
    val coachInsightContent: StateFlow<String> = _coachInsightContent.asStateFlow()

    private val _isLoadingCoachInsight = MutableStateFlow(false)
    val isLoadingCoachInsight: StateFlow<Boolean> = _isLoadingCoachInsight.asStateFlow()

    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    private val _selectedPlayerSummary = MutableStateFlow<LeaderboardPlayer?>(null)
    val selectedPlayerSummary: StateFlow<LeaderboardPlayer?> = _selectedPlayerSummary.asStateFlow()

    private val _selectedMetricDetail = MutableStateFlow<PerformanceMetric?>(null)
    val selectedMetricDetail: StateFlow<PerformanceMetric?> = _selectedMetricDetail.asStateFlow()

    private val _showCalibrationFlow = MutableStateFlow(false)
    val showCalibrationFlow: StateFlow<Boolean> = _showCalibrationFlow.asStateFlow()

    private val _showScoringWorksSheet = MutableStateFlow(false)
    val showScoringWorksSheet: StateFlow<Boolean> = _showScoringWorksSheet.asStateFlow()

    // Settings & preferences
    val soundCuesEnabled = MutableStateFlow(true)
    val hapticsEnabled = MutableStateFlow(true)
    val selectedDuration = MutableStateFlow("5 min")

    // Filter chip in Train
    val trainFilter = MutableStateFlow("All")

    // Compete Leaderboard Tab
    val leaderboardTab = MutableStateFlow("Global") // Global, Country, Friends

    // Progress Time Range
    val progressTimeRange = MutableStateFlow("7D") // 7D, 30D, 90D, All

    // User Profile
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Notifications List
    private val _notifications = MutableStateFlow(
        listOf(
            NotificationItem("1", "New personal best!", "You hit 214 ms in Classic Reaction yesterday.", "2h ago", true),
            NotificationItem("2", "Daily challenge is live", "Flash Grid challenge ends in 08:42:16. Compete now.", "5h ago", true),
            NotificationItem("3", "7-day streak achieved!", "Consistency bonus applied: +12 RPI points.", "1d ago", false),
            NotificationItem("4", "Weekly baseline updated", "Your median improved by 12 ms this week.", "2d ago", false)
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Drills Catalogue
    val coreDrills = listOf(
        DrillInfo(
            type = DrillType.CLASSIC,
            subtitle = "Visual · 5 trials",
            badge = "Best 214 ms",
            purpose = "Measure simple sensory reaction to sudden visual stimulus."
        ),
        DrillInfo(
            type = DrillType.CHOICE,
            subtitle = "Decision · 8 min",
            badge = "Recommended",
            purpose = "Train motor inhibition and multi-choice decision speed under pressure."
        ),
        DrillInfo(
            type = DrillType.PRECISION,
            subtitle = "Precision · 30 sec",
            badge = "Best 91%",
            purpose = "Evaluate accurate motor targeting and rapid target acquisition."
        ),
        DrillInfo(
            type = DrillType.FLASH_GRID,
            subtitle = "Peripheral · 45 sec",
            badge = "Daily mode",
            purpose = "Test spatial reflex and wide-angle peripheral awareness."
        )
    )

    // Leaderboards
    val globalPlayers = listOf(
        LeaderboardPlayer(1, "Marcus Chen", "@chen_m", "M", "🇸🇬", "178 ms", true, rpi = 912, bestDrill = "Classic: 174 ms"),
        LeaderboardPlayer(2, "Elena Rostova", "@elena_fps", "E", "🇪🇪", "184 ms", true, rpi = 895, bestDrill = "Flash Grid: 98%"),
        LeaderboardPlayer(3, "Takeru Sato", "@tksato", "T", "🇯🇵", "189 ms", true, rpi = 884, bestDrill = "Choice: 212 ms"),
        LeaderboardPlayer(4, "Liam O'Connor", "@liam_reflex", "L", "🇮🇪", "195 ms", true, rpi = 862, bestDrill = "Precision: 96%"),
        LeaderboardPlayer(18492, "Alex Morgan", "@alexplays", "A", "🇮🇳", "214 ms", true, isCurrentUser = true, rpi = 742, bestDrill = "Classic: 214 ms")
    )

    val countryPlayers = listOf(
        LeaderboardPlayer(1, "Aarav Sharma", "@aarav_s", "A", "🇮🇳", "192 ms", true, rpi = 870, bestDrill = "Classic: 192 ms"),
        LeaderboardPlayer(2, "Priya Nair", "@priyan", "P", "🇮🇳", "198 ms", true, rpi = 851, bestDrill = "Flash Grid: 94%"),
        LeaderboardPlayer(3, "Rohan Verma", "@rohan_v", "R", "🇮🇳", "205 ms", true, rpi = 810, bestDrill = "Choice: 228 ms"),
        LeaderboardPlayer(142, "Alex Morgan", "@alexplays", "A", "🇮🇳", "214 ms", true, isCurrentUser = true, rpi = 742, bestDrill = "Classic: 214 ms")
    )

    val friendPlayers = listOf(
        LeaderboardPlayer(1, "David Miller", "@dmiller", "D", "🇺🇸", "208 ms", true, rpi = 760, bestDrill = "Classic: 208 ms"),
        LeaderboardPlayer(2, "Alex Morgan", "@alexplays", "A", "🇮🇳", "214 ms", true, isCurrentUser = true, rpi = 742, bestDrill = "Classic: 214 ms"),
        LeaderboardPlayer(3, "Sarah Lin", "@sarah_l", "S", "🇨🇦", "225 ms", true, rpi = 720, bestDrill = "Choice: 242 ms")
    )

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        _activeScreen.value = ActiveScreen.TABS
    }

    fun openDrillDetail(drill: DrillInfo) {
        _selectedDrillForSheet.value = drill
    }

    fun closeDrillDetail() {
        _selectedDrillForSheet.value = null
    }

    fun startDrill(type: DrillType, dailyMode: Boolean = false) {
        _activeDrillType.value = type
        _isDailyMode.value = dailyMode
        _selectedDrillForSheet.value = null
        _activeScreen.value = ActiveScreen.ACTIVE_DRILL
    }

    fun exitActiveDrill() {
        _activeScreen.value = ActiveScreen.TABS
    }

    fun completeDrillRun(
        type: DrillType,
        medianMs: Long,
        bestMs: Long,
        accuracyPercent: Int,
        consistencyMs: Long
    ) {
        val currentProfile = _userProfile.value
        val baseline = currentProfile.baselineMs
        val diffFromBaseline = ((baseline - medianMs).toFloat() / baseline.toFloat()) * 100f
        val isPb = medianMs < currentProfile.fastestMs

        viewModelScope.launch {
            val coachNote = GeminiCoachService.analyzeDrillRun(
                drillTitle = type.title,
                medianMs = medianMs,
                accuracyPercent = accuracyPercent,
                consistencyMs = consistencyMs
            )

            val result = DrillRunResult(
                drillType = type,
                medianTimeMs = medianMs,
                bestTimeMs = bestMs,
                accuracyPercent = accuracyPercent,
                consistencyMs = consistencyMs,
                vsBaselinePercent = (diffFromBaseline * 10f).roundToInt() / 10f,
                isPersonalBest = isPb,
                coachNote = coachNote,
                isVerified = true
            )
            _lastResult.value = result

            // Insert to Room
            dao.insertSession(
                SessionEntity(
                    drillId = type.id,
                    drillTitle = type.title,
                    timestamp = System.currentTimeMillis(),
                    medianTimeMs = medianMs,
                    accuracyPercent = accuracyPercent,
                    consistencyMs = consistencyMs,
                    isVerified = true,
                    note = coachNote
                )
            )

            // Update user profile
            val newTotal = currentProfile.totalSessions + 1
            val newFastest = if (isPb) medianMs else currentProfile.fastestMs
            val deltaRpi = if (medianMs < baseline) 4 else 1
            _userProfile.value = currentProfile.copy(
                totalSessions = newTotal,
                fastestMs = newFastest,
                rpi = currentProfile.rpi + deltaRpi,
                weeklyDelta = "+${24 + deltaRpi} this week"
            )

            _activeScreen.value = ActiveScreen.RESULT
        }
    }

    fun openCoachInsight() {
        _showCoachInsightSheet.value = true
        if (_coachInsightContent.value.isEmpty()) {
            _isLoadingCoachInsight.value = true
            viewModelScope.launch {
                val insight = GeminiCoachService.getCoachInsightExplanation(
                    visualSpeedMs = _userProfile.value.fastestMs,
                    choiceSpeedMs = 286
                )
                _coachInsightContent.value = insight
                _isLoadingCoachInsight.value = false
            }
        }
    }

    fun closeCoachInsight() {
        _showCoachInsightSheet.value = false
    }

    fun openNotifications() {
        _showNotificationsSheet.value = true
    }

    fun closeNotifications() {
        _showNotificationsSheet.value = false
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isUnread = false) }
    }

    fun openPlayerSummary(player: LeaderboardPlayer) {
        _selectedPlayerSummary.value = player
    }

    fun closePlayerSummary() {
        _selectedPlayerSummary.value = null
    }

    fun openMetricDetail(metric: PerformanceMetric) {
        _selectedMetricDetail.value = metric
    }

    fun closeMetricDetail() {
        _selectedMetricDetail.value = null
    }

    fun openCalibration() {
        _showCalibrationFlow.value = true
    }

    fun closeCalibration() {
        _showCalibrationFlow.value = false
    }

    fun openScoringWorks() {
        _showScoringWorksSheet.value = true
    }

    fun closeScoringWorks() {
        _showScoringWorksSheet.value = false
    }
}
