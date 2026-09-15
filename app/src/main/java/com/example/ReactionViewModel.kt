package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiCoachService
import com.example.data.ReactionDatabase
import com.example.data.SessionEntity
import com.example.model.AthleteProfile
import com.example.model.DrillCategory
import com.example.model.DrillInfo
import com.example.model.DrillRunResult
import com.example.model.DrillType
import com.example.model.LeaderboardPlayer
import com.example.model.NotificationItem
import com.example.model.PerformanceMetric
import com.example.model.SportsBattery
import com.example.model.TrialRecord
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    // User Profile
    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = "Alex Morgan",
            handle = "@alexplays",
            country = "India",
            countryFlag = "🇮🇳",
            sport = "Motorsport / GT3 Driver",
            rpi = 742,
            rankLabel = "Top 18%",
            weeklyDelta = "+24 this week",
            streakDays = 7,
            baselineMs = 248,
            fastestMs = 214,
            totalSessions = 24,
            memberSince = "Aug 2026"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow(
        listOf(
            NotificationItem(
                id = "1",
                title = "Coach Combine Verified",
                description = "Your Motorsport Combine baseline was logged with 5.8% CV stability.",
                timeAgo = "10m ago",
                isUnread = true
            ),
            NotificationItem(
                id = "2",
                title = "Daily Streak Maintained",
                description = "Day 7 completed! Neuromuscular reaction latency is up 4.2% this week.",
                timeAgo = "2h ago",
                isUnread = true
            ),
            NotificationItem(
                id = "3",
                title = "Global Leaderboard Update",
                description = "Elena moved to #2 in Flash Grid. You are #18,492 worldwide.",
                timeAgo = "1d ago",
                isUnread = false
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Filters and Screen Preferences
    private val _trainFilter = MutableStateFlow("All")
    val trainFilter: StateFlow<String> = _trainFilter.asStateFlow()

    private val _leaderboardTab = MutableStateFlow("Global")
    val leaderboardTab: StateFlow<String> = _leaderboardTab.asStateFlow()

    private val _progressTimeRange = MutableStateFlow("30D")
    val progressTimeRange: StateFlow<String> = _progressTimeRange.asStateFlow()

    private val _selectedDuration = MutableStateFlow("30s")
    val selectedDuration: StateFlow<String> = _selectedDuration.asStateFlow()

    private val _soundCuesEnabled = MutableStateFlow(true)
    val soundCuesEnabled: StateFlow<Boolean> = _soundCuesEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _exportCsvContent = MutableStateFlow("")
    val exportCsvContent: StateFlow<String> = _exportCsvContent.asStateFlow()

    fun setTrainFilter(filter: String) { _trainFilter.value = filter }
    fun setLeaderboardTab(tab: String) { _leaderboardTab.value = tab }
    fun setProgressTimeRange(range: String) { _progressTimeRange.value = range }
    fun setSelectedDuration(duration: String) { _selectedDuration.value = duration }
    fun setSoundCuesEnabled(enabled: Boolean) { _soundCuesEnabled.value = enabled }
    fun setHapticsEnabled(enabled: Boolean) { _hapticsEnabled.value = enabled }

    // Coach & Team Management Sheets
    private val _showRosterSheet = MutableStateFlow(false)
    val showRosterSheet: StateFlow<Boolean> = _showRosterSheet.asStateFlow()

    private val _showBatterySheet = MutableStateFlow(false)
    val showBatterySheet: StateFlow<Boolean> = _showBatterySheet.asStateFlow()

    private val _selectedBattery = MutableStateFlow<SportsBattery?>(null)
    val selectedBattery: StateFlow<SportsBattery?> = _selectedBattery.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    // Complete Professional Neuro-Athletic Drill Library (12 Proven Protocols in 5 Categories)
    val coreDrills = listOf(
        // --- 1. SENSORY REFLEX ---
        DrillInfo(
            type = DrillType.CLASSIC,
            subtitle = "Simple Visual Latency (SRT)",
            badge = "BENCHMARK",
            purpose = "Measure raw photon-to-digitizer latency. The international gold standard for human neurological reaction speed.",
            protocolDetails = "5 trials with randomized delay (1.8s - 3.8s). Penalizes anticipations under 100ms per IAAF Olympic rules.",
            defaultDuration = "1 min",
            categoryEnum = DrillCategory.SENSORY,
            targetBenchmark = "< 200 ms",
            difficulty = "Foundational",
            levelNumber = 1
        ),
        DrillInfo(
            type = DrillType.AUDITORY,
            subtitle = "Acoustic Reflex Conduction",
            badge = "OLYMPIC TONE",
            purpose = "Measures auditory pathway conduction speed, which bypasses visual retinal photoreceptor transduction.",
            protocolDetails = "Pure 880Hz acoustic tone. Screen remains neutral to isolate and train acoustic startle reflex.",
            defaultDuration = "1 min",
            categoryEnum = DrillCategory.SENSORY,
            targetBenchmark = "< 160 ms",
            difficulty = "Intermediate",
            levelNumber = 2
        ),
        DrillInfo(
            type = DrillType.F1_LIGHTS,
            subtitle = "Formula 1 Start Gantry",
            badge = "MOTORSPORT",
            purpose = "5 red lights ignite in sequence, extinguishing at an unpredictable interval. Tests sudden release reflex.",
            protocolDetails = "FIA Gantry simulation. Hold focus through sequential illumination, strike the microsecond lights shut off.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.SENSORY,
            targetBenchmark = "< 195 ms",
            difficulty = "Elite",
            levelNumber = 3
        ),

        // --- 2. COGNITIVE & DECISION ---
        DrillInfo(
            type = DrillType.CHOICE,
            subtitle = "Directional Decision Latency (CRT)",
            badge = "HICK'S LAW",
            purpose = "Quantify cognitive overhead when selecting between left or right directional vectors under extreme time pressure.",
            protocolDetails = "2-choice spatial vector discrimination. Quantifies millisecond overhead added by decision bifurcation.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            targetBenchmark = "< 260 ms",
            difficulty = "Intermediate",
            levelNumber = 2
        ),
        DrillInfo(
            type = DrillType.STROOP,
            subtitle = "Stroop Color-Word Conflict",
            badge = "EXECUTIVE LOAD",
            purpose = "Overrides automatic semantic reading to respond to ink color. Stretches cognitive executive processing speed.",
            protocolDetails = "Semantic vs chromatic interference. Tap the button matching the displayed ink color, not the word.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            targetBenchmark = "< 360 ms",
            difficulty = "Advanced",
            levelNumber = 3
        ),
        DrillInfo(
            type = DrillType.EVEN_ODD,
            subtitle = "Numerical Parity Discrimination",
            badge = "DUAL-CHANNEL",
            purpose = "Tests split-second mathematical category discrimination. Rapid cognitive classification under microsecond pressure.",
            protocolDetails = "High-speed randomized digits flash. Immediately classify parity (Even vs Odd) withouthesitation.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            targetBenchmark = "< 320 ms",
            difficulty = "Intermediate",
            levelNumber = 2
        ),

        // --- 3. MOTOR INHIBITION & CONTROL ---
        DrillInfo(
            type = DrillType.GO_NO_GO,
            subtitle = "Impulse Suppression & Inhibition",
            badge = "STOP-SIGNAL",
            purpose = "Evaluates prefrontal cortex ability to suppress motor action on false distractors (Amber No-Go vs Green Go).",
            protocolDetails = "75% Go frequency creates high habitual motor bias; 25% No-Go probes commission error suppression.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.INHIBITION,
            targetBenchmark = "0 Errors, <240ms",
            difficulty = "Advanced",
            levelNumber = 3
        ),
        DrillInfo(
            type = DrillType.FLANKER,
            subtitle = "Eriksen Flanker Attention",
            badge = "SELECTIVE FOCUS",
            purpose = "Isolates central visual target while resisting distracting, conflicting directional arrows flanking the center.",
            protocolDetails = "5-arrow stimulus arrays. Strike center arrow vector while filtering out congruent or incongruent flankers.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.INHIBITION,
            targetBenchmark = "< 310 ms",
            difficulty = "Elite",
            levelNumber = 4
        ),

        // --- 4. VISUAL & PERIPHERAL FIELD ---
        DrillInfo(
            type = DrillType.FLASH_GRID,
            subtitle = "3x3 Parafoveal Awareness",
            badge = "UFOV FIELD",
            purpose = "Measures speed of spatial attention deployment across 9 peripheral visual fields while holding gaze fixation.",
            protocolDetails = "Randomized grid stimulus flashes demanding instant peripheral identification with fixed central anchor.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.VISUAL,
            targetBenchmark = "< 300 ms",
            difficulty = "Intermediate",
            levelNumber = 2
        ),
        DrillInfo(
            type = DrillType.PRECISION,
            subtitle = "Saccadic Motor Targeting",
            badge = "SACCADIC GAZE",
            purpose = "Tests eye-hand coordination by acquiring and striking randomized spatial parafoveal targets with pinpoint accuracy.",
            protocolDetails = "Evaluates motor target acquisition latency and spatial accuracy across varying screen coordinates.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.VISUAL,
            targetBenchmark = "< 250 ms",
            difficulty = "Advanced",
            levelNumber = 3
        ),

        // --- 5. NEUROMUSCULAR & RHYTHM ---
        DrillInfo(
            type = DrillType.CNS_TAP,
            subtitle = "10s Motor Velocity & Readiness",
            badge = "CNS READINESS",
            purpose = "Evaluates central nervous system fatigue and neuromuscular firing frequency in Hz over a 10s maximum burst.",
            protocolDetails = "10-second rapid tapping cadence. Measures first 5s vs last 5s velocity decay slope and motor stability.",
            defaultDuration = "10s",
            categoryEnum = DrillCategory.NEUROMUSCULAR,
            targetBenchmark = "> 7.5 Hz",
            difficulty = "Power Test",
            levelNumber = 2
        ),
        DrillInfo(
            type = DrillType.RHYTHM_SYNC,
            subtitle = "Coincidence Anticipation Timing",
            badge = "TEMPORAL 0ms",
            purpose = "Quantifies coincidence anticipation timing precision. Tests internal clock synchronization with external kinematics.",
            protocolDetails = "Velocity cursor sweeps toward the baseline. Strike at the exact millisecond of arrival (0 ms coincidence).",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.NEUROMUSCULAR,
            targetBenchmark = "± 12 ms",
            difficulty = "Elite Precision",
            levelNumber = 4
        )
    )

    // Team Roster & Active Athlete
    val rosterList = listOf(
        AthleteProfile(
            id = "alex",
            name = "Alex Morgan",
            handle = "@alexplays",
            sport = "Motorsport",
            role = "GT3 Driver",
            avatarInitial = "A",
            countryFlag = "🇮🇳",
            rpi = 742,
            baselineMs = 248,
            fastestMs = 214,
            cnsTapBaselineHz = 7.6f,
            totalSessions = 24,
            isActive = true
        ),
        AthleteProfile(
            id = "marcus",
            name = "Marcus Chen",
            handle = "@chen_fps",
            sport = "Esports",
            role = "Tactical FPS Pro",
            avatarInitial = "M",
            countryFlag = "🇸🇬",
            rpi = 912,
            baselineMs = 186,
            fastestMs = 174,
            cnsTapBaselineHz = 8.4f,
            totalSessions = 58,
            isActive = false
        ),
        AthleteProfile(
            id = "sarah",
            name = "Sarah Lin",
            handle = "@sarah_box",
            sport = "Combat Sports",
            role = "Featherweight Boxing",
            avatarInitial = "S",
            countryFlag = "🇨🇦",
            rpi = 780,
            baselineMs = 230,
            fastestMs = 198,
            cnsTapBaselineHz = 7.8f,
            totalSessions = 39,
            isActive = false
        ),
        AthleteProfile(
            id = "mateo",
            name = "Mateo Silva",
            handle = "@silva_m",
            sport = "Field Soccer",
            role = "Central Midfielder",
            avatarInitial = "M",
            countryFlag = "🇧🇷",
            rpi = 805,
            baselineMs = 218,
            fastestMs = 196,
            cnsTapBaselineHz = 7.9f,
            totalSessions = 31,
            isActive = false
        )
    )

    private val _activeAthlete = MutableStateFlow(rosterList[0])
    val activeAthlete: StateFlow<AthleteProfile> = _activeAthlete.asStateFlow()

    // Sports Batteries (Coach Combine Protocols)
    val sportsBatteries = listOf(
        SportsBattery(
            id = "f1_combine",
            title = "Motorsport Vigilance Combine",
            sportTag = "Motorsport / F1",
            description = "Evaluates F1 gantry release, dual-target steering decisions, and 3x3 peripheral awareness under split-second mental load.",
            drills = listOf(DrillType.F1_LIGHTS, DrillType.CHOICE, DrillType.FLASH_GRID),
            durationMin = "3 min",
            benchmark = "Sub-200ms F1 release, >92% choice accuracy"
        ),
        SportsBattery(
            id = "combat_reflex",
            title = "Combat Fighter Reflex Matrix",
            sportTag = "Boxing / MMA",
            description = "Measures auditory starter anticipation and impulse inhibition control to counter false punches and strike on true openings.",
            drills = listOf(DrillType.AUDITORY, DrillType.GO_NO_GO),
            durationMin = "2 min",
            benchmark = "Sub-160ms auditory, 0 false alarms on No-Go"
        ),
        SportsBattery(
            id = "executive_circuit",
            title = "Executive Cognitive Speed Circuit",
            sportTag = "Esports / Tactical",
            description = "High-interference cognitive stress test pairing Stroop chromatic interference with Eriksen Flanker selective focus.",
            drills = listOf(DrillType.STROOP, DrillType.FLANKER, DrillType.EVEN_ODD),
            durationMin = "3 min",
            benchmark = ">94% accuracy with sub-330ms cognitive latency"
        ),
        SportsBattery(
            id = "cns_readiness",
            title = "Pre-Match CNS Neural Readiness",
            sportTag = "All High Performance",
            description = "10-second rapid finger tapping frequency compared with simple visual reflex to screen for central nervous system fatigue.",
            drills = listOf(DrillType.CNS_TAP, DrillType.CLASSIC),
            durationMin = "2 min",
            benchmark = ">7.5 Hz tap rate with <8% CV stability"
        ),
        SportsBattery(
            id = "field_agility",
            title = "Field Sport Saccadic Battery",
            sportTag = "Soccer / Basketball",
            description = "Evaluates motor inhibition against offside traps, randomized peripheral targets, and rapid spatial targeting.",
            drills = listOf(DrillType.GO_NO_GO, DrillType.FLASH_GRID, DrillType.PRECISION),
            durationMin = "4 min",
            benchmark = "95% accuracy with sub-260ms decision speed"
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

    // Coach Athlete Roster controls
    fun openRoster() {
        _showRosterSheet.value = true
    }

    fun closeRoster() {
        _showRosterSheet.value = false
    }

    fun switchAthlete(athlete: AthleteProfile) {
        _activeAthlete.value = athlete
        _userProfile.value = _userProfile.value.copy(
            name = athlete.name,
            handle = athlete.handle,
            countryFlag = athlete.countryFlag,
            sport = "${athlete.sport} / ${athlete.role}",
            rpi = athlete.rpi,
            baselineMs = athlete.baselineMs,
            fastestMs = athlete.fastestMs,
            totalSessions = athlete.totalSessions
        )
        _showRosterSheet.value = false
    }

    // Sports Batteries (Coach Combine)
    fun openBatteryDetail(battery: SportsBattery) {
        _selectedBattery.value = battery
        _showBatterySheet.value = true
    }

    fun closeBatteryDetail() {
        _selectedBattery.value = null
        _showBatterySheet.value = false
    }

    fun startBatteryFirstDrill(battery: SportsBattery) {
        _showBatterySheet.value = false
        val firstDrill = battery.drills.firstOrNull() ?: DrillType.CLASSIC
        startDrill(firstDrill, dailyMode = false)
    }

    // Raw Coach Data Export
    fun openExportDialog() {
        _exportCsvContent.value = generateCoachExportCsv()
        _showExportDialog.value = true
    }

    fun closeExportDialog() {
        _showExportDialog.value = false
    }

    fun generateCoachExportCsv(): String {
        val currentSessions = sessions.value
        val sb = StringBuilder()
        sb.append("Timestamp,Date_ISO,Athlete,Sport,Drill_ID,Drill_Title,Median_ms,Best_ms,Accuracy_pct,Consistency_ms,CV_pct,False_Starts,Raw_Trials_CSV,Verified\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        currentSessions.forEach { s ->
            val dateStr = sdf.format(Date(s.timestamp))
            sb.append("${s.timestamp},\"$dateStr\",\"${s.athleteName}\",\"${s.sportCategory}\",\"${s.drillId}\",\"${s.drillTitle}\",${s.medianTimeMs},${s.bestTimeMs},${s.accuracyPercent},${s.consistencyMs},${s.cvPercent},${s.falseStarts},\"${s.rawTrialsCsv}\",${s.isVerified}\n")
        }
        return sb.toString()
    }

    fun completeDrillRun(
        type: DrillType,
        medianMs: Long,
        bestMs: Long,
        accuracyPercent: Int,
        consistencyMs: Long,
        cvPercent: Float = 6.4f,
        falseStarts: Int = 0,
        iesScore: Long = medianMs,
        exGaussianTau: Long = 20L,
        cnsHz: Float? = null,
        rawTrials: List<TrialRecord> = emptyList()
    ) {
        val athlete = _activeAthlete.value
        val baseline = athlete.baselineMs
        val diffFromBaseline = if (baseline > 0) {
            ((baseline - medianMs).toFloat() / baseline.toFloat()) * 100f
        } else 0f
        val isPb = medianMs < athlete.fastestMs && medianMs > 100L

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
                cvPercent = cvPercent,
                falseStarts = falseStarts,
                inverseEfficiencyScore = iesScore,
                exGaussianTau = exGaussianTau,
                cnsFrequencyHz = cnsHz,
                vsBaselinePercent = (diffFromBaseline * 10f).roundToInt() / 10f,
                isPersonalBest = isPb,
                coachNote = coachNote,
                isVerified = true,
                athleteName = athlete.name,
                rawTrials = rawTrials
            )
            _lastResult.value = result

            // Serialize raw trials for Room export
            val rawCsv = if (rawTrials.isNotEmpty()) {
                rawTrials.joinToString(";") { "${it.trialIndex}:${it.latencyMs}ms:${if (it.isCorrect) "OK" else "ERR"}" }
            } else "$medianMs"

            // Insert to Room
            dao.insertSession(
                SessionEntity(
                    drillId = type.id,
                    drillTitle = type.title,
                    timestamp = System.currentTimeMillis(),
                    medianTimeMs = medianMs,
                    bestTimeMs = bestMs,
                    accuracyPercent = accuracyPercent,
                    consistencyMs = consistencyMs,
                    cvPercent = cvPercent,
                    falseStarts = falseStarts,
                    athleteName = athlete.name,
                    sportCategory = athlete.sport,
                    isVerified = true,
                    rawTrialsCsv = rawCsv,
                    note = coachNote
                )
            )

            // Update user profile
            val currentProfile = _userProfile.value
            val newTotal = currentProfile.totalSessions + 1
            val newFastest = if (isPb) medianMs else currentProfile.fastestMs
            val deltaRpi = if (medianMs < baseline && medianMs > 100) 4 else 1
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
