package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiCoachService
import com.example.data.CalibrationStore
import com.example.data.ReactionDatabase
import com.example.data.SessionEntity
import com.example.model.BatteryAssessmentResult
import com.example.model.BatteryDrillScore
import com.example.model.BatteryProtocolType
import com.example.model.DrillCategory
import com.example.model.DrillInfo
import com.example.model.DrillMode
import com.example.model.supportsTrainMode
import com.example.model.DrillRunResult
import com.example.model.DrillType
import com.example.model.drillTypeFromId
import com.example.model.isChoiceCategory
import com.example.model.NotificationItem
import com.example.model.PerformanceMetric
import com.example.model.SensoryInput
import com.example.model.SportsBattery
import com.example.model.TaskComplexity
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

    private val _activeDrillMode = MutableStateFlow(DrillMode.TEST)
    val activeDrillMode: StateFlow<DrillMode> = _activeDrillMode.asStateFlow()

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

    private val _selectedMetricDetail = MutableStateFlow<PerformanceMetric?>(null)
    val selectedMetricDetail: StateFlow<PerformanceMetric?> = _selectedMetricDetail.asStateFlow()

    private val _showCalibrationFlow = MutableStateFlow(false)
    val showCalibrationFlow: StateFlow<Boolean> = _showCalibrationFlow.asStateFlow()

    // Timing calibration for this device, applied to every new measurement.
    private val _touchSamplingOffsetMs = MutableStateFlow(CalibrationStore.touchSamplingOffsetMs(application))
    val touchSamplingOffsetMs: StateFlow<Long> = _touchSamplingOffsetMs.asStateFlow()

    private val _panelLatencyMs = MutableStateFlow(CalibrationStore.panelLatencyMs(application))
    val panelLatencyMs: StateFlow<Long> = _panelLatencyMs.asStateFlow()

    private val _displayLatencyMs = MutableStateFlow(CalibrationStore.totalOffsetMs(application))
    val displayLatencyMs: StateFlow<Long> = _displayLatencyMs.asStateFlow()

    fun saveCalibration(touchSamplingMs: Long, panelMs: Long, refreshHz: Int) {
        val app = getApplication<Application>()
        CalibrationStore.save(app, touchSamplingMs, panelMs, refreshHz)
        _touchSamplingOffsetMs.value = CalibrationStore.touchSamplingOffsetMs(app)
        _panelLatencyMs.value = CalibrationStore.panelLatencyMs(app)
        _displayLatencyMs.value = CalibrationStore.totalOffsetMs(app)
    }

    private val _showScoringWorksSheet = MutableStateFlow(false)
    val showScoringWorksSheet: StateFlow<Boolean> = _showScoringWorksSheet.asStateFlow()

    // User Profile (identity fields only; performance stats are derived from real session history below)
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Notifications are generated from real events (personal bests, streaks) as they happen.
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            sessions.collect { history ->
                // Profile stats describe measured performance, so only Test runs count.
                val measured = history.filter { it.mode == DrillMode.TEST.name }
                if (measured.isNotEmpty()) {
                    syncProfileWithHistory(measured)
                }
            }
        }
    }

    private fun syncProfileWithHistory(history: List<SessionEntity>) {
        val fastest = history.minOf { s -> if (s.bestTimeMs > 0) s.bestTimeMs else s.medianTimeMs }

        // Baseline is a rolling window, not your first-ever run.
        //
        // Anchoring to the first session compared every future result against the run
        // where you were still learning the buttons, so "vs baseline" was flattering by
        // construction and never got harder. The median of your earliest few sessions is
        // a fairer reference point.
        val baselineWindow = history.takeLast(5).map { it.medianTimeMs }.sorted()
        val baseline = if (baselineWindow.isEmpty()) 0L else baselineWindow[baselineWindow.size / 2]

        val streak = computeStreakDays(history)

        // RPI is built on recent median form, not a single lucky trial.
        //
        // "1000 - fastest" keyed the headline number to the noisiest statistic in the
        // set - the one trial most likely to be a near-anticipation that squeaked past
        // 100ms - and once set it could never come back down. The median of the last
        // five sessions moves in both directions and reflects form rather than a fluke.
        val recentMedians = history.take(5).map { it.medianTimeMs }.sorted()
        val recentForm = if (recentMedians.isEmpty()) 0L else recentMedians[recentMedians.size / 2]
        val rpi = if (recentForm <= 0L) 0 else (1000L - recentForm).toInt().coerceIn(0, 999)

        val sdf = SimpleDateFormat("MMM yyyy", Locale.US)
        val memberSince = sdf.format(Date(history.last().timestamp))

        val weeklyDelta = computeWeeklyDelta(history)

        val current = _userProfile.value
        _userProfile.value = current.copy(
            totalSessions = history.size,
            fastestMs = fastest,
            baselineMs = baseline,
            streakDays = streak,
            rpi = rpi,
            weeklyDelta = weeklyDelta,
            memberSince = memberSince
        )
    }

    private fun computeStreakDays(history: List<SessionEntity>): Int {
        val dayMillis = 24L * 60L * 60L * 1000L
        val days = history.map { it.timestamp / dayMillis }.toSortedSet().sortedDescending()
        if (days.isEmpty()) return 0
        val today = System.currentTimeMillis() / dayMillis
        if (days.first() != today && days.first() != today - 1) return 0
        var streak = 1
        for (i in 0 until days.size - 1) {
            if (days[i] - days[i + 1] == 1L) streak++ else break
        }
        return streak
    }

    private fun computeWeeklyDelta(history: List<SessionEntity>): String {
        val weekMillis = 7L * 24L * 60L * 60L * 1000L
        val now = System.currentTimeMillis()
        val thisWeek = history.filter { now - it.timestamp <= weekMillis }
        val lastWeek = history.filter { now - it.timestamp > weekMillis && now - it.timestamp <= weekMillis * 2 }
        if (thisWeek.isEmpty() || lastWeek.isEmpty()) return ""
        val avgThis = thisWeek.map { it.medianTimeMs }.average()
        val avgLast = lastWeek.map { it.medianTimeMs }.average()
        val diff = (avgLast - avgThis).roundToInt()
        return when {
            diff > 0 -> "-$diff ms this week"
            diff < 0 -> "+${-diff} ms this week"
            else -> "No change this week"
        }
    }

    // Filters and Screen Preferences
    private val _trainFilter = MutableStateFlow("All")
    val trainFilter: StateFlow<String> = _trainFilter.asStateFlow()

    private val _progressTimeRange = MutableStateFlow("30D")
    val progressTimeRange: StateFlow<String> = _progressTimeRange.asStateFlow()


    private val _soundCuesEnabled = MutableStateFlow(true)
    val soundCuesEnabled: StateFlow<Boolean> = _soundCuesEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _exportCsvContent = MutableStateFlow("")
    val exportCsvContent: StateFlow<String> = _exportCsvContent.asStateFlow()

    fun setTrainFilter(filter: String) { _trainFilter.value = filter }
    fun setProgressTimeRange(range: String) { _progressTimeRange.value = range }
    fun setSoundCuesEnabled(enabled: Boolean) { _soundCuesEnabled.value = enabled }
    fun setHapticsEnabled(enabled: Boolean) { _hapticsEnabled.value = enabled }

    // Coach & Team Management Sheets
    private val _showBatterySheet = MutableStateFlow(false)
    val showBatterySheet: StateFlow<Boolean> = _showBatterySheet.asStateFlow()

    private val _selectedBattery = MutableStateFlow<SportsBattery?>(null)
    val selectedBattery: StateFlow<SportsBattery?> = _selectedBattery.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    data class BatterySessionState(
        val battery: SportsBattery,
        val drillResults: MutableMap<DrillType, BatteryDrillScore> = mutableMapOf(),
        var currentCombineIndex: Int = 0,
        var isCombineActive: Boolean = false
    )

    private val _batterySessionState = MutableStateFlow<BatterySessionState?>(null)
    val batterySessionState: StateFlow<BatterySessionState?> = _batterySessionState.asStateFlow()

    private val _batteryAssessmentResult = MutableStateFlow<BatteryAssessmentResult?>(null)
    val batteryAssessmentResult: StateFlow<BatteryAssessmentResult?> = _batteryAssessmentResult.asStateFlow()

    private val _showBatteryResultSheet = MutableStateFlow(false)
    val showBatteryResultSheet: StateFlow<Boolean> = _showBatteryResultSheet.asStateFlow()

    // Complete Professional Neuro-Athletic Drill Library (13 Proven Protocols across Complexity & Sensory Inputs)
    val coreDrills = listOf(
        // --- 1. SENSORY REFLEX (Simple Reaction Time - SRT) ---
        DrillInfo(
            type = DrillType.CLASSIC,
            subtitle = "Simple Visual Latency (SRT)",
            badge = "BENCHMARK",
            purpose = "Measure raw photon-to-digitizer latency. The international gold standard for human neurological reaction speed.",
            protocolDetails = "5 trials with randomized delay (1.8s - 3.8s). Penalizes anticipations under 100ms per IAAF Olympic rules.",
            defaultDuration = "1 min",
            categoryEnum = DrillCategory.SENSORY,
            complexity = TaskComplexity.SRT,
            sensoryInput = SensoryInput.VISUAL,
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
            complexity = TaskComplexity.SRT,
            sensoryInput = SensoryInput.AUDITORY,
            targetBenchmark = "< 160 ms",
            difficulty = "Intermediate",
            levelNumber = 2
        ),
        DrillInfo(
            type = DrillType.TACTILE,
            subtitle = "Somatosensory Haptic Reflex",
            badge = "HAPTIC PULSE",
            purpose = "Measures somatosensory tactile pathway latency. Triggered purely by skin mechanoreceptors and physical vibration.",
            protocolDetails = "Neutral dark screen. Instant microsecond impulse triggered by sudden vibration burst without visual warning.",
            defaultDuration = "1 min",
            categoryEnum = DrillCategory.SENSORY,
            complexity = TaskComplexity.SRT,
            sensoryInput = SensoryInput.TACTILE,
            targetBenchmark = "< 180 ms",
            difficulty = "Foundational",
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
            complexity = TaskComplexity.SRT,
            sensoryInput = SensoryInput.VISUAL,
            targetBenchmark = "< 195 ms",
            difficulty = "Elite",
            levelNumber = 3
        ),

        // --- 2. COGNITIVE & DECISION (Choice Reaction Time - CRT) ---
        DrillInfo(
            type = DrillType.CHOICE,
            subtitle = "Directional Decision Latency (CRT)",
            badge = "HICK'S LAW",
            purpose = "Quantify cognitive overhead when selecting between left or right directional vectors under extreme time pressure.",
            protocolDetails = "2-choice spatial vector discrimination. Quantifies millisecond overhead added by decision bifurcation.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            complexity = TaskComplexity.CRT,
            sensoryInput = SensoryInput.VISUAL,
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
            complexity = TaskComplexity.CRT,
            sensoryInput = SensoryInput.VISUAL,
            targetBenchmark = "< 360 ms",
            difficulty = "Advanced",
            levelNumber = 3
        ),
        DrillInfo(
            type = DrillType.EVEN_ODD,
            subtitle = "Numerical Parity Discrimination",
            badge = "DUAL-CHANNEL",
            purpose = "Tests split-second mathematical category discrimination. Rapid cognitive classification under microsecond pressure.",
            protocolDetails = "High-speed randomized digits flash. Immediately classify parity (Even vs Odd) without hesitation.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            complexity = TaskComplexity.CRT,
            sensoryInput = SensoryInput.VISUAL,
            targetBenchmark = "< 320 ms",
            difficulty = "Intermediate",
            levelNumber = 2
        ),
        DrillInfo(
            type = DrillType.CHOICE_4WAY,
            subtitle = "4-Way Cardinal Arrow Keys (CRT)",
            badge = "SPATIAL VECTORS",
            purpose = "Measures 4-choice directional discrimination across cardinal axes (Up, Down, Left, Right). Eliminates single-axis motor anticipation.",
            protocolDetails = "Randomized cardinal arrow appears. Strike matching directional pad. Strict IAAF <100ms false start and MAD outlier filtering.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            complexity = TaskComplexity.CRT,
            sensoryInput = SensoryInput.VISUAL,
            targetBenchmark = "< 330 ms",
            difficulty = "Intermediate",
            levelNumber = 2
        ),
        DrillInfo(
            type = DrillType.COLOR_MATCH,
            subtitle = "Chromatic Color Matching Choice (CRT)",
            badge = "COLOR STIMULUS",
            purpose = "Measures 4-way chromatic stimulus-to-motor translation speed. Tests visual cortex hue discrimination and response selection.",
            protocolDetails = "Screen flashes Red, Blue, Green, or Amber. Strike corresponding color tile. Bypasses directional motor bias.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            complexity = TaskComplexity.CRT,
            sensoryInput = SensoryInput.VISUAL,
            targetBenchmark = "< 340 ms",
            difficulty = "Intermediate",
            levelNumber = 2
        ),
        DrillInfo(
            type = DrillType.GRID_TRACKING,
            subtitle = "4x4 Matrix Grid Tracking (CRT)",
            badge = "16-NODE MATRIX",
            purpose = "Expands spatial choice reaction from 1D/2D buttons to an entire 16-cell interactive matrix. Demands visual search + choice targeting.",
            protocolDetails = "Active coordinate flashes across 4x4 matrix. Direct digitizer contact timestamped with hardware timer.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            complexity = TaskComplexity.CRT,
            sensoryInput = SensoryInput.VISUAL,
            targetBenchmark = "< 400 ms",
            difficulty = "Advanced",
            levelNumber = 3
        ),
        DrillInfo(
            type = DrillType.SPATIAL_AUDIO,
            subtitle = "Spatial Binaural Audio Vector (CRT)",
            badge = "BINAURAL STEREO",
            purpose = "Evaluates interaural time difference and auditory choice speed. Stimulus is acoustic (Left vs Right earbud channel) with motor choice.",
            protocolDetails = "Sub-millisecond acoustic impulse fired in Left or Right channel. Athlete selects corresponding ear trigger.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            complexity = TaskComplexity.CRT,
            sensoryInput = SensoryInput.AUDITORY,
            targetBenchmark = "< 320 ms",
            difficulty = "Intermediate",
            levelNumber = 3
        ),
        DrillInfo(
            type = DrillType.QUADRANT_CHOICE,
            subtitle = "4-Quadrant Flashing Choice (CRT)",
            badge = "QUADRANT SPEED",
            purpose = "Large-field 4-quadrant choice reaction. One of four visual screen quadrants flashes; direct touch capture.",
            protocolDetails = "Visual field quadrant activation with zero distraction. Measures split-field spatial choice latency.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.COGNITIVE,
            complexity = TaskComplexity.CRT,
            sensoryInput = SensoryInput.VISUAL,
            targetBenchmark = "< 350 ms",
            difficulty = "Foundational CRT",
            levelNumber = 2
        ),

        // --- 3. MOTOR INHIBITION & CONTROL (Recognition Reaction Time - RRT / Go-NoGo) ---
        DrillInfo(
            type = DrillType.GO_NO_GO,
            subtitle = "Impulse Suppression & Inhibition",
            badge = "STOP-SIGNAL",
            purpose = "Evaluates prefrontal cortex ability to suppress motor action on false distractors (Amber No-Go vs Green Go).",
            protocolDetails = "75% Go frequency creates high habitual motor bias; 25% No-Go probes commission error suppression.",
            defaultDuration = "2 min",
            categoryEnum = DrillCategory.INHIBITION,
            complexity = TaskComplexity.RRT,
            sensoryInput = SensoryInput.VISUAL,
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
            complexity = TaskComplexity.RRT,
            sensoryInput = SensoryInput.VISUAL,
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
            complexity = TaskComplexity.RRT,
            sensoryInput = SensoryInput.VISUAL,
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
            complexity = TaskComplexity.RRT,
            sensoryInput = SensoryInput.VISUAL,
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
            complexity = TaskComplexity.SRT,
            sensoryInput = SensoryInput.KINETIC,
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
            complexity = TaskComplexity.SRT,
            sensoryInput = SensoryInput.KINETIC,
            targetBenchmark = "± 12 ms",
            difficulty = "Elite Precision",
            levelNumber = 4
        )
    )

    // Sports Batteries (Coach Combine Protocols)
    val sportsBatteries = listOf(
        SportsBattery(
            id = "single_type_crt_battery",
            title = "Specialized CRT Battery",
            sportTag = "Choice reaction · 4 interfaces",
            description = "The scientific gold-standard for Choice Reaction Time (CRT). 4 specialized drills testing the same cognitive capacity across different interfaces (4-Way Arrows, Color Matching, 4x4 Grid Matrix, Spatial Audio). Calculates true cognitive speed via Average of Medians: (M1 + M2 + M3 + M4) / 4.",
            drills = listOf(DrillType.CHOICE_4WAY, DrillType.COLOR_MATCH, DrillType.GRID_TRACKING, DrillType.SPATIAL_AUDIO),
            durationMin = "4 min",
            benchmark = "Average CRT < 350 ms across 4 interfaces",
            protocolType = BatteryProtocolType.SINGLE_TYPE_SPECIALIZED
        ),
        SportsBattery(
            id = "multi_type_matrix_battery",
            title = "Comprehensive Reaction Matrix",
            sportTag = "Multi-paradigm index",
            description = "Multi-paradigm battery combining Simple (SRT), Choice (CRT), and Recognition (RRT) speeds. Normalizes scores against standard human baselines to calculate a composite Reaction Index (0-100 score).",
            drills = listOf(DrillType.CLASSIC, DrillType.QUADRANT_CHOICE, DrillType.GO_NO_GO, DrillType.AUDITORY, DrillType.FLASH_GRID),
            durationMin = "5 min",
            benchmark = "Reaction Index > 85/100 (Top 15% Athlete)",
            protocolType = BatteryProtocolType.MULTI_TYPE_MATRIX,
            baselineNormMs = mapOf(
                DrillType.CLASSIC to 200L,
                DrillType.QUADRANT_CHOICE to 400L,
                DrillType.GO_NO_GO to 320L,
                DrillType.AUDITORY to 160L,
                DrillType.FLASH_GRID to 300L
            )
        ),
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

    fun startDrill(type: DrillType, dailyMode: Boolean = false, mode: DrillMode = DrillMode.TEST) {
        _activeDrillType.value = type
        _isDailyMode.value = dailyMode
        _activeDrillMode.value = if (type.supportsTrainMode) mode else DrillMode.TEST
        _selectedDrillForSheet.value = null
        _activeScreen.value = ActiveScreen.ACTIVE_DRILL
    }

    fun exitActiveDrill() {
        _activeScreen.value = ActiveScreen.TABS
    }


    // Sports Batteries (Coach Combine & Battery Studio Grid)
    fun openBatteryDetail(battery: SportsBattery) {
        _selectedBattery.value = battery
        if (_batterySessionState.value?.battery?.id != battery.id) {
            _batterySessionState.value = BatterySessionState(battery = battery)
        }
        _showBatterySheet.value = true
    }

    fun closeBatteryDetail() {
        _selectedBattery.value = null
        _showBatterySheet.value = false
    }

    fun startBatteryFirstDrill(battery: SportsBattery) {
        startBatteryCombine(battery)
    }

    fun startBatteryDrill(battery: SportsBattery, drillType: DrillType) {
        _selectedBattery.value = battery
        val state = _batterySessionState.value ?: BatterySessionState(battery = battery)
        _batterySessionState.value = state.copy(isCombineActive = false)
        _showBatterySheet.value = false
        startDrill(drillType, dailyMode = false)
    }

    fun startBatteryCombine(battery: SportsBattery) {
        _selectedBattery.value = battery
        val state = _batterySessionState.value ?: BatterySessionState(battery = battery)
        val firstUncompleted = battery.drills.firstOrNull { state.drillResults[it]?.isCompleted != true }
            ?: battery.drills.first()
        val index = battery.drills.indexOf(firstUncompleted).coerceAtLeast(0)
        _batterySessionState.value = state.copy(
            currentCombineIndex = index,
            isCombineActive = true
        )
        _showBatterySheet.value = false
        startDrill(firstUncompleted, dailyMode = false)
    }

    fun advanceBatteryCombine() {
        val state = _batterySessionState.value ?: return
        val nextUncompleted = state.battery.drills.firstOrNull { state.drillResults[it]?.isCompleted != true }
        if (nextUncompleted != null) {
            val idx = state.battery.drills.indexOf(nextUncompleted)
            _batterySessionState.value = state.copy(currentCombineIndex = idx, isCombineActive = true)
            startDrill(nextUncompleted, dailyMode = false)
        } else {
            // All completed!
            calculateAndSetBatteryResult(state.battery)
            _activeScreen.value = ActiveScreen.TABS
            _showBatteryResultSheet.value = true
        }
    }

    fun resetBattery(battery: SportsBattery) {
        _batterySessionState.value = BatterySessionState(battery = battery)
        _batteryAssessmentResult.value = null
    }

    fun openBatteryAssessment(result: BatteryAssessmentResult) {
        _batteryAssessmentResult.value = result
        _showBatteryResultSheet.value = true
    }

    fun closeBatteryAssessment() {
        _showBatteryResultSheet.value = false
    }

    fun calculateAndSetBatteryResult(battery: SportsBattery): BatteryAssessmentResult {
        val state = _batterySessionState.value ?: BatterySessionState(battery = battery)
        val athlete = _userProfile.value
        // Only include drills the athlete actually ran; never fabricate a score for a skipped drill.
        val completedScores = battery.drills.mapNotNull { drill -> state.drillResults[drill] }

        val result = if (battery.protocolType == BatteryProtocolType.SINGLE_TYPE_SPECIALIZED) {
            // Concept 1: Average of Medians across all specialized interfaces
            val sumMedians = completedScores.sumOf { it.medianMs }
            val avgMedian = (sumMedians.toDouble() / completedScores.size.coerceAtLeast(1)).toFloat()
            val roundedAvg = (avgMedian * 10f).roundToInt() / 10f

            val formula = "Final CRT Score = (${completedScores.joinToString(" + ") { "${it.medianMs}ms" }}) / ${completedScores.size} = ${roundedAvg.roundToInt()} ms"

            val rank = when {
                roundedAvg < 280f -> "S-Tier Elite CRT"
                roundedAvg < 330f -> "A-Tier Fast CRT"
                roundedAvg < 380f -> "B-Tier Solid CRT"
                else -> "Foundational CRT"
            }

            val summary = "Evaluated cognitive decision latency across ${completedScores.size} distinct interfaces (4-Way Cardinal, Chromatic Color Matching, 4x4 Grid Matrix, Spatial Audio). Measures true cognitive processing invariance without single-interface muscle memory bias."

            BatteryAssessmentResult(
                batteryId = battery.id,
                batteryTitle = battery.title,
                protocolType = battery.protocolType,
                athleteName = athlete.name,
                drillScores = completedScores,
                finalScoreValue = roundedAvg,
                finalScoreUnit = "ms",
                rankTitle = rank,
                analysisSummary = summary,
                calculationFormula = formula
            )
        } else {
            // Concept 2: Normalized Composite Reaction Index (0-100)
            val sumNormalized = completedScores.sumOf { it.normalizedScore }
            val avgIndex = (sumNormalized.toDouble() / completedScores.size.coerceAtLeast(1)).toFloat()
            val roundedIndex = (avgIndex * 10f).roundToInt() / 10f

            val formula = "Reaction Index = (${completedScores.joinToString(" + ") { "${it.normalizedScore}" }}) / ${completedScores.size} = ${roundedIndex.roundToInt()} / 100"

            val rank = when {
                roundedIndex >= 90f -> "S-Rank Olympic Tier (Top 5%)"
                roundedIndex >= 80f -> "A-Rank Advanced Athlete (Top 15%)"
                roundedIndex >= 70f -> "B-Rank Competitive (Top 30%)"
                else -> "Standard Population Baseline"
            }

            val summary = "Multi-paradigm battery combining Simple (SRT), Choice (CRT), and Recognition (RRT). Each drill was normalized against population baselines to calculate a holistic neuro-athletic readiness index."

            BatteryAssessmentResult(
                batteryId = battery.id,
                batteryTitle = battery.title,
                protocolType = battery.protocolType,
                athleteName = athlete.name,
                drillScores = completedScores,
                finalScoreValue = roundedIndex,
                finalScoreUnit = "/ 100",
                rankTitle = rank,
                analysisSummary = summary,
                calculationFormula = formula
            )
        }

        _batteryAssessmentResult.value = result
        return result
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
        rawTrials: List<TrialRecord> = emptyList(),
        mode: DrillMode = DrillMode.TEST,
        survivedSec: Int = 0,
        levelReached: Int = 0
    ) {
        val athlete = _userProfile.value
        // Train runs are excluded from every comparison: their difficulty ramps within
        // the run, so their times are not measurements and must not move a baseline,
        // a personal best or the trend.
        val isTest = mode == DrillMode.TEST
        val history = sessions.value.filter { it.mode == DrillMode.TEST.name }
        // Personal baseline is this athlete's own first-ever recorded session (real), not a fabricated constant.
        val baseline = history.lastOrNull()?.medianTimeMs ?: medianMs
        val diffFromBaseline = if (baseline > 0) {
            ((baseline - medianMs).toFloat() / baseline.toFloat()) * 100f
        } else 0f
        val previousFastest = history.minOfOrNull { s -> if (s.bestTimeMs > 0) s.bestTimeMs else s.medianTimeMs }
        val isPb = isTest && (previousFastest == null || bestMs < previousFastest) && bestMs > 100L

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
                mode = mode,
                survivedSec = survivedSec,
                levelReached = levelReached,
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
                    note = coachNote,
                    mode = mode.name,
                    survivedSec = survivedSec,
                    levelReached = levelReached
                )
            )

            // Profile stats (totalSessions, fastestMs, rpi, streak, etc.) are recomputed
            // automatically from real Room history by the sessions collector in init{}.

            if (isPb) {
                _notifications.value = listOf(
                    NotificationItem(
                        id = "pb_${System.currentTimeMillis()}",
                        title = "New Personal Best",
                        description = "${type.title}: $bestMs ms — your fastest result yet.",
                        timeAgo = "Just now",
                        isUnread = true
                    )
                ) + _notifications.value
            }

            // Record into active battery if present
            val batteryState = _batterySessionState.value
            if (batteryState != null && batteryState.battery.drills.contains(type)) {
                val normScore = if (batteryState.battery.protocolType == BatteryProtocolType.MULTI_TYPE_MATRIX) {
                    val base = batteryState.battery.baselineNormMs[type] ?: 300L
                    val ratio = (medianMs - base).toFloat() / base.toFloat()
                    (100f - ratio * 60f).roundToInt().coerceIn(10, 100)
                } else 0

                batteryState.drillResults[type] = BatteryDrillScore(
                    drillType = type,
                    medianMs = medianMs,
                    bestMs = bestMs,
                    accuracyPercent = accuracyPercent,
                    consistencyMs = consistencyMs,
                    isCompleted = true,
                    falseStarts = falseStarts,
                    normalizedScore = normScore
                )

                val allCompleted = batteryState.battery.drills.all { batteryState.drillResults[it]?.isCompleted == true }
                if (allCompleted) {
                    calculateAndSetBatteryResult(batteryState.battery)
                }
            }

            _activeScreen.value = ActiveScreen.RESULT
        }
    }

    // Real average median time across completed choice-reaction (CRT) drills, or null if none recorded yet.
    fun averageChoiceSpeedMs(): Long? {
        val choiceSessions = sessions.value.filter { s -> drillTypeFromId(s.drillId)?.isChoiceCategory == true }
        if (choiceSessions.isEmpty()) return null
        return choiceSessions.map { it.medianTimeMs }.average().roundToInt().toLong()
    }

    fun openCoachInsight() {
        _showCoachInsightSheet.value = true
        if (_coachInsightContent.value.isEmpty()) {
            val visualSpeedMs = _userProfile.value.fastestMs
            val choiceSpeedMs = averageChoiceSpeedMs()
            if (visualSpeedMs <= 0 || choiceSpeedMs == null) {
                _coachInsightContent.value = "Complete a Visual Reflex drill and a Choice Reaction drill to unlock this comparison."
                return
            }
            _isLoadingCoachInsight.value = true
            viewModelScope.launch {
                val insight = GeminiCoachService.getCoachInsightExplanation(
                    visualSpeedMs = visualSpeedMs,
                    choiceSpeedMs = choiceSpeedMs
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

    fun openMetricDetail(metric: PerformanceMetric) {
        _selectedMetricDetail.value = metric
    }

    fun closeMetricDetail() {
        _selectedMetricDetail.value = null
    }

    /** Discard a run so an interrupted or mis-tapped session cannot distort the trend. */
    fun deleteSession(id: Long) {
        viewModelScope.launch {
            dao.deleteSession(id)
        }
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
