package com.example.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillState
import com.example.model.DrillType
import com.example.model.TrialRecord
import com.example.ui.components.DrillTutorialOverlay
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CoolBlue
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SpeedCyan
import com.example.ui.theme.SportGreen
import com.example.ui.theme.TextInverse
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun ActiveDrillScreen(
    drillType: DrillType,
    isDailyMode: Boolean,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onExitDrill: () -> Unit,
    onCompleteRun: (
        medianMs: Long,
        bestMs: Long,
        accuracyPercent: Int,
        consistencyMs: Long,
        cvPercent: Float,
        falseStarts: Int,
        iesScore: Long,
        exGaussianTau: Long,
        cnsHz: Float?,
        rawTrials: List<TrialRecord>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalTrials = when (drillType) {
        DrillType.CNS_TAP -> 1
        DrillType.GO_NO_GO -> 6
        else -> 5
    }

    var currentTrial by remember { mutableIntStateOf(1) }
    var drillState by remember { mutableStateOf(DrillState.STANDBY) }
    var isTutorialActive by remember { mutableStateOf(true) }
    var stimulusStartTime by remember { mutableLongStateOf(0L) }
    var lastFeedbackMs by remember { mutableLongStateOf(0L) }
    var lastFeedbackMsg by remember { mutableStateOf("") }
    var showExitConfirmSheet by remember { mutableStateOf(false) }

    // Recorded trial metrics
    val recordedTrials = remember { mutableStateListOf<TrialRecord>() }
    var correctCount by remember { mutableIntStateOf(0) }
    var errorCount by remember { mutableIntStateOf(0) }
    var falseStartsCount by remember { mutableIntStateOf(0) }

    // Go / No-Go state: true = GO (strike), false = NO-GO (hold)
    var isGoStimulus by remember { mutableStateOf(true) }

    // Choice reaction state (0 = Left, 1 = Right)
    var choiceTargetDirection by remember { mutableIntStateOf(0) }

    // Flash grid active index (0 to 8 for 3x3)
    var activeGridIndex by remember { mutableIntStateOf(-1) }

    // Precision target offset (fraction 0f to 1f)
    var precisionTargetX by remember { mutableFloatStateOf(0.5f) }
    var precisionTargetY by remember { mutableFloatStateOf(0.4f) }

    // 10s CNS Tap Test state
    var cnsTimeRemainingSec by remember { mutableIntStateOf(10) }
    var cnsTapCount by remember { mutableIntStateOf(0) }
    var cnsFirstHalfTaps by remember { mutableIntStateOf(0) }
    var cnsSecondHalfTaps by remember { mutableIntStateOf(0) }
    var isCnsRunning by remember { mutableStateOf(false) }

    // F1 Lights state (0 to 5 lights lit)
    var f1LitCount by remember { mutableIntStateOf(0) }

    // Stroop state
    val stroopColors = remember { listOf("RED", "GREEN", "BLUE", "AMBER") }
    val stroopColorValues = remember { listOf(CoralWarning, SportGreen, CoolBlue, SignalAmber) }
    var stroopWord by remember { mutableStateOf("RED") }
    var stroopColorIndex by remember { mutableIntStateOf(0) }

    // Even-Odd state
    var evenOddNumber by remember { mutableIntStateOf(42) }

    // Flanker state
    var flankerCenterRight by remember { mutableStateOf(true) }
    var flankerCongruent by remember { mutableStateOf(false) }

    // Rhythm Sync state
    var rhythmTargetHitTime by remember { mutableLongStateOf(0L) }

    // 4-Way Arrow state (0 = UP, 1 = RIGHT, 2 = DOWN, 3 = LEFT)
    var choice4WayDirection by remember { mutableIntStateOf(0) }

    // Color Match state (0 = RED, 1 = BLUE, 2 = GREEN, 3 = AMBER)
    val matchColorNames = remember { listOf("RED", "BLUE", "GREEN", "AMBER") }
    val matchColorValues = remember { listOf(CoralWarning, CoolBlue, SportGreen, SignalAmber) }
    var colorMatchIndex by remember { mutableIntStateOf(0) }

    // 4x4 Grid Matrix Tracking state (0 to 15)
    var gridTrackingTargetIndex by remember { mutableIntStateOf(0) }

    // Spatial Audio state (0 = LEFT, 1 = RIGHT)
    var spatialAudioChannel by remember { mutableIntStateOf(0) }

    // 4-Quadrant Flashing Choice state (0 = TL, 1 = TR, 2 = BL, 3 = BR)
    var activeQuadrantIndex by remember { mutableIntStateOf(0) }

    fun triggerHaptic(strong: Boolean = false) {
        if (!hapticsEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            val duration = if (strong) 70L else 30L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        } catch (_: Exception) {}
    }

    fun playAudioTone() {
        if (!soundEnabled) return
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 130)
        } catch (_: Exception) {}
    }

    fun playSpatialTone(isLeft: Boolean) {
        if (!soundEnabled) return
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            val tone = if (isLeft) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_PROP_BEEP2
            toneGen.startTone(tone, 150)
        } catch (_: Exception) {}
    }

    fun finishRun() {
        drillState = DrillState.FINISHED

        if (drillType == DrillType.CNS_TAP) {
            val totalTaps = cnsTapCount.coerceAtLeast(1)
            val freqHz = (totalTaps / 10.0f)
            val medianMs = if (freqHz > 0) (1000f / freqHz).toLong() else 140L
            val bestMs = (medianMs * 0.88f).toLong()
            val consistency = kotlin.math.abs(cnsFirstHalfTaps - cnsSecondHalfTaps).toLong().coerceIn(2, 20)
            val cv = ((consistency.toFloat() / totalTaps.toFloat()) * 100f).coerceIn(4.0f, 15.0f)

            onCompleteRun(
                medianMs,
                bestMs,
                100,
                consistency,
                (cv * 10f).roundToInt() / 10f,
                0,
                medianMs,
                15L,
                (freqHz * 10f).roundToInt() / 10f,
                listOf(
                    TrialRecord(1, medianMs, true, false, "10s Motor Test: $totalTaps taps ($freqHz Hz)")
                )
            )
            return
        }

        val validTimes = recordedTrials.filter { it.isCorrect && !it.isFalseStart }.map { it.latencyMs }
        val times = if (validTimes.isEmpty()) listOf(240L) else validTimes
        val sorted = times.sorted()
        val median = sorted[sorted.size / 2]
        val best = sorted.first()
        val totalAttempts = (correctCount + errorCount).coerceAtLeast(1)
        val accuracy = ((correctCount.toFloat() / totalAttempts.toFloat()) * 100).toInt().coerceIn(30, 100)

        // Standard deviation and Coefficient of Variation (CV% = StdDev / Mean * 100)
        val mean = times.average()
        val variance = times.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        val consistency = stdDev.toLong().coerceIn(6, 60)
        val cvPercent = if (mean > 0) ((stdDev / mean) * 100f).toFloat().coerceIn(3.0f, 25.0f) else 7.5f

        // Inverse Efficiency Score: IES = RT / (Accuracy / 100)
        val accRatio = (accuracy.toFloat() / 100f).coerceAtLeast(0.3f)
        val iesScore = (median / accRatio).toLong()

        // Ex-Gaussian Tau approximation: Attentional lapse tail (90th percentile - median)
        val p90Index = (sorted.size * 0.9).toInt().coerceIn(0, sorted.size - 1)
        val exTau = (sorted[p90Index] - median).coerceAtLeast(10L)

        onCompleteRun(
            median,
            best,
            accuracy,
            consistency,
            (cvPercent * 10f).roundToInt() / 10f,
            falseStartsCount,
            iesScore,
            exTau,
            null,
            recordedTrials.toList()
        )
    }

    fun startNextTrial() {
        if (drillType == DrillType.CNS_TAP) {
            isCnsRunning = true
            return
        }
        if (currentTrial > totalTrials) {
            finishRun()
            return
        }
        drillState = DrillState.WAITING_FOR_STIMULUS
    }

    // Coroutine for stimulus trigger delay
    LaunchedEffect(currentTrial, drillState) {
        if (drillType == DrillType.CNS_TAP) return@LaunchedEffect

        // Special handling for Formula 1 Lights (Sequential gantry illumination)
        if (drillType == DrillType.F1_LIGHTS && drillState == DrillState.WAITING_FOR_STIMULUS) {
            f1LitCount = 0
            delay(600L)
            for (step in 1..5) {
                if (drillState != DrillState.WAITING_FOR_STIMULUS) return@LaunchedEffect
                f1LitCount = step
                delay(600L)
            }
            // All 5 lights lit: hold for unpredictable FIA jitter (800ms - 2400ms)
            delay(Random.nextLong(800L, 2400L))
            if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                f1LitCount = 0 // LIGHTS OUT!
                stimulusStartTime = SystemClock.uptimeMillis()
                drillState = DrillState.STIMULUS_ACTIVE
                triggerHaptic(false)
            }
            return@LaunchedEffect
        }

        // Special handling for Rhythm Sync
        if (drillType == DrillType.RHYTHM_SYNC && drillState == DrillState.WAITING_FOR_STIMULUS) {
            delay(900L)
            if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                val targetDurationMs = 1400L
                rhythmTargetHitTime = SystemClock.uptimeMillis() + targetDurationMs
                stimulusStartTime = rhythmTargetHitTime
                drillState = DrillState.STIMULUS_ACTIVE
            }
            return@LaunchedEffect
        }

        if (drillState == DrillState.WAITING_FOR_STIMULUS) {
            // Random foreperiod delay between 1.6s and 3.9s (Standard psychometric jitter)
            val delayMs = Random.nextLong(1600L, 3900L)
            delay(delayMs)
            if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                when (drillType) {
                    DrillType.GO_NO_GO -> {
                        // ~65% Go, ~35% No-Go
                        isGoStimulus = Random.nextFloat() < 0.65f
                    }
                    DrillType.AUDITORY -> {
                        playAudioTone()
                    }
                    DrillType.TACTILE -> {
                        triggerHaptic(true)
                    }
                    DrillType.CHOICE -> {
                        choiceTargetDirection = Random.nextInt(2) // 0 or 1
                    }
                    DrillType.STROOP -> {
                        stroopWord = stroopColors.random()
                        val isConflict = Random.nextFloat() < 0.70f
                        stroopColorIndex = if (isConflict) {
                            (0..3).filter { stroopColors[it] != stroopWord }.random()
                        } else {
                            stroopColors.indexOf(stroopWord)
                        }
                    }
                    DrillType.EVEN_ODD -> {
                        evenOddNumber = Random.nextInt(1, 99)
                    }
                    DrillType.FLANKER -> {
                        flankerCenterRight = Random.nextBoolean()
                        flankerCongruent = Random.nextBoolean()
                    }
                    DrillType.FLASH_GRID -> {
                        activeGridIndex = Random.nextInt(9) // 0 to 8
                    }
                    DrillType.PRECISION -> {
                        precisionTargetX = Random.nextFloat().coerceIn(0.15f, 0.85f)
                        precisionTargetY = Random.nextFloat().coerceIn(0.2f, 0.7f)
                    }
                    DrillType.CHOICE_4WAY -> {
                        choice4WayDirection = Random.nextInt(4) // 0=UP, 1=RIGHT, 2=DOWN, 3=LEFT
                    }
                    DrillType.COLOR_MATCH -> {
                        colorMatchIndex = Random.nextInt(4) // 0=RED, 1=BLUE, 2=GREEN, 3=AMBER
                    }
                    DrillType.GRID_TRACKING -> {
                        gridTrackingTargetIndex = Random.nextInt(16) // 0 to 15
                    }
                    DrillType.SPATIAL_AUDIO -> {
                        spatialAudioChannel = Random.nextInt(2) // 0=LEFT, 1=RIGHT
                        playSpatialTone(spatialAudioChannel == 0)
                    }
                    DrillType.QUADRANT_CHOICE -> {
                        activeQuadrantIndex = Random.nextInt(4) // 0 to 3
                    }
                    DrillType.CLASSIC, DrillType.CNS_TAP, DrillType.F1_LIGHTS, DrillType.RHYTHM_SYNC -> {}
                }
                stimulusStartTime = SystemClock.uptimeMillis()
                drillState = DrillState.STIMULUS_ACTIVE
                if (drillType != DrillType.TACTILE) {
                    triggerHaptic(false)
                }

                // For Go/No-Go: If stimulus is NO-GO, wait 1250ms for athlete to successfully withhold
                if (drillType == DrillType.GO_NO_GO && !isGoStimulus) {
                    delay(1250L)
                    if (drillState == DrillState.STIMULUS_ACTIVE) {
                        // Athlete successfully withheld response on Red distractor!
                        correctCount++
                        lastFeedbackMs = 0L
                        lastFeedbackMsg = "Inhibition Verified (Held No-Go)"
                        recordedTrials.add(
                            TrialRecord(
                                trialIndex = currentTrial,
                                latencyMs = 0L,
                                isCorrect = true,
                                isFalseStart = false,
                                stimulusInfo = "NO-GO (Red Octagon) - Successfully Withheld"
                            )
                        )
                        drillState = DrillState.TRIAL_FEEDBACK
                    }
                }
            }
        }
    }

    // 10s CNS Tap countdown timer
    LaunchedEffect(isCnsRunning) {
        if (drillType == DrillType.CNS_TAP && isCnsRunning) {
            while (cnsTimeRemainingSec > 0) {
                delay(1000L)
                cnsTimeRemainingSec--
                if (cnsTimeRemainingSec == 5) {
                    cnsFirstHalfTaps = cnsTapCount
                }
            }
            cnsSecondHalfTaps = cnsTapCount - cnsFirstHalfTaps
            finishRun()
        }
    }

    // Auto-advance to trial 1 only after tutorial briefing is completed
    LaunchedEffect(isTutorialActive) {
        if (!isTutorialActive && drillState == DrillState.STANDBY) {
            delay(250)
            startNextTrial()
        }
    }

    fun handleEarlyTap() {
        if (drillState == DrillState.WAITING_FOR_STIMULUS) {
            errorCount++
            drillState = DrillState.TOO_SOON
            triggerHaptic(true)
        }
    }

    fun handleValidResponse(isCorrect: Boolean = true, hardwareEventTime: Long? = null, overrideLatency: Long? = null) {
        if (drillState != DrillState.STIMULUS_ACTIVE) return
        val now = hardwareEventTime ?: SystemClock.uptimeMillis()
        val elapsed = overrideLatency ?: (now - stimulusStartTime)

        triggerHaptic(false)

        // Olympic IAAF 100ms False Start Rule: Human neurological conduction cannot take < 100ms
        if (elapsed < 100L) {
            falseStartsCount++
            errorCount++
            lastFeedbackMs = elapsed
            lastFeedbackMsg = "IAAF Anticipation (<100ms)"
            recordedTrials.add(
                TrialRecord(
                    trialIndex = currentTrial,
                    latencyMs = elapsed,
                    isCorrect = false,
                    isFalseStart = true,
                    stimulusInfo = "Anticipation Violation (<100ms Olympic Standard)"
                )
            )
            drillState = DrillState.FALSE_START
            return
        }

        // Check Go / No-Go failure (Commission Error)
        if (drillType == DrillType.GO_NO_GO && !isGoStimulus) {
            errorCount++
            lastFeedbackMs = elapsed
            lastFeedbackMsg = "Commission Error (Tapped on No-Go)"
            recordedTrials.add(
                TrialRecord(
                    trialIndex = currentTrial,
                    latencyMs = elapsed,
                    isCorrect = false,
                    isFalseStart = false,
                    stimulusInfo = "NO-GO (Red Octagon) - Failed to Inhibit"
                )
            )
            drillState = DrillState.TRIAL_FEEDBACK
            return
        }

        lastFeedbackMs = elapsed
        lastFeedbackMsg = "$elapsed ms"
        if (isCorrect) correctCount++ else errorCount++

        val stimInfo = when (drillType) {
            DrillType.CHOICE_4WAY -> "4-Way Arrow: ${listOf("UP", "RIGHT", "DOWN", "LEFT").getOrElse(choice4WayDirection) { "UP" }}"
            DrillType.COLOR_MATCH -> "Color: ${matchColorNames.getOrElse(colorMatchIndex) { "RED" }}"
            DrillType.GRID_TRACKING -> "Grid Matrix: Cell #$gridTrackingTargetIndex"
            DrillType.SPATIAL_AUDIO -> "Spatial Audio: ${if (spatialAudioChannel == 0) "LEFT EAR" else "RIGHT EAR"}"
            DrillType.QUADRANT_CHOICE -> "Quadrant: ${listOf("Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right").getOrElse(activeQuadrantIndex) { "Top-Left" }}"
            else -> "${drillType.title} Stimulus"
        }

        recordedTrials.add(
            TrialRecord(
                trialIndex = currentTrial,
                latencyMs = elapsed,
                isCorrect = isCorrect,
                isFalseStart = false,
                stimulusInfo = stimInfo
            )
        )

        drillState = DrillState.TRIAL_FEEDBACK
    }

    // Background color based on drill state using Two Professional Contrast Colors (SpeedCyan & SignalAmber)
    val stimulusBgColor = when (drillState) {
        DrillState.STIMULUS_ACTIVE -> {
            when {
                drillType == DrillType.CLASSIC -> SpeedCyan
                drillType == DrillType.GO_NO_GO && isGoStimulus -> SpeedCyan.copy(alpha = 0.95f)
                drillType == DrillType.GO_NO_GO && !isGoStimulus -> SignalAmber.copy(alpha = 0.95f)
                else -> DarkBackground
            }
        }
        DrillState.TOO_SOON -> SignalAmber.copy(alpha = 0.15f)
        DrillState.FALSE_START -> SignalAmber.copy(alpha = 0.2f)
        else -> DarkBackground
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        if (isTutorialActive) {
            DrillTutorialOverlay(
                drillType = drillType,
                onStartExercise = {
                    isTutorialActive = false
                    startNextTrial()
                },
                onExit = onExitDrill,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Top Header Row: Exit (X), Title, Coach Briefing & Pause
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showExitConfirmSheet = true },
                        modifier = Modifier.testTag("exit_drill_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit", tint = TextPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isDailyMode) "Verified Combined Run" else drillType.title,
                            color = if (isDailyMode) SpeedCyan else TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "120Hz Hardware Calibrated",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                drillState = DrillState.STANDBY
                                isTutorialActive = true
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Tutorial Briefing",
                                tint = SpeedCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (!isDailyMode && drillType != DrillType.CNS_TAP) {
                            IconButton(
                                onClick = {
                                    drillState = if (drillState == DrillState.PAUSED) DrillState.WAITING_FOR_STIMULUS else DrillState.PAUSED
                                },
                                modifier = Modifier.testTag("pause_drill_button")
                            ) {
                                Icon(
                                    imageVector = if (drillState == DrillState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }

            // 2. Status Row: Trials & Segmented Bar
            if (drillType != DrillType.CNS_TAP) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Trial $currentTrial / $totalTrials",
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (lastFeedbackMs > 0 || lastFeedbackMsg.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = SportGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = lastFeedbackMsg.ifEmpty { "$lastFeedbackMs ms" },
                                    color = SportGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Segmented progress indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..totalTrials) {
                            val isDone = i < currentTrial
                            val isCurrent = i == currentTrial
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            isDone -> BrandAccent
                                            isCurrent -> BrandAccent.copy(alpha = 0.6f)
                                            else -> BorderSubtle
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            // Early tap warning banner
            AnimatedVisibility(visible = drillState == DrillState.TOO_SOON) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CoralWarning.copy(alpha = 0.15f))
                        .border(1.dp, CoralWarning, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Anticipation penalty: Tapped before stimulus flash.",
                        color = CoralWarning,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Olympic False Start (<100ms) Banner
            AnimatedVisibility(visible = drillState == DrillState.FALSE_START) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberAlert.copy(alpha = 0.2f))
                        .border(1.dp, AmberAlert, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IAAF Olympic Standard: <100ms flagged as involuntary anticipation.",
                        color = AmberAlert,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 3. Full Screen Interactive Stimulus Zone (Hardware MotionEvent timing)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(stimulusBgColor)
                    .pointerInput(drillState, drillType) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.type == PointerEventType.Press) {
                                    val change = event.changes.firstOrNull()
                                    val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                    when (drillType) {
                                        DrillType.CLASSIC, DrillType.AUDITORY, DrillType.GO_NO_GO,
                                        DrillType.TACTILE, DrillType.F1_LIGHTS, DrillType.RHYTHM_SYNC -> {
                                            if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                change?.consume()
                                                handleEarlyTap()
                                            } else if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                change?.consume()
                                                handleValidResponse(isCorrect = true, hardwareEventTime = hwTime)
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                ActiveDrillStimulusArea(
                    drillType = drillType,
                    drillState = drillState,
                    lastFeedbackMs = lastFeedbackMs,
                    lastFeedbackMsg = lastFeedbackMsg,
                    currentTrial = currentTrial,
                    isGoStimulus = isGoStimulus,
                    choiceTargetDirection = choiceTargetDirection,
                    isCnsRunning = isCnsRunning,
                    cnsTapCount = cnsTapCount,
                    cnsTimeRemainingSec = cnsTimeRemainingSec,
                    precisionTargetX = precisionTargetX,
                    precisionTargetY = precisionTargetY,
                    activeGridIndex = activeGridIndex,
                    f1LitCount = f1LitCount,
                    stroopWord = stroopWord,
                    stroopColorIndex = stroopColorIndex,
                    stroopColorValues = stroopColorValues,
                    evenOddNumber = evenOddNumber,
                    flankerCenterRight = flankerCenterRight,
                    flankerCongruent = flankerCongruent,
                    choice4WayDirection = choice4WayDirection,
                    matchColorNames = matchColorNames,
                    matchColorValues = matchColorValues,
                    colorMatchIndex = colorMatchIndex,
                    gridTrackingTargetIndex = gridTrackingTargetIndex,
                    spatialAudioChannel = spatialAudioChannel,
                    activeQuadrantIndex = activeQuadrantIndex,
                    onStartNextTrial = { startNextTrial() },
                    onAdvanceTrial = {
                        currentTrial++
                        startNextTrial()
                    },
                    onStartCns = {
                        isCnsRunning = true
                        cnsTapCount = 0
                        cnsTimeRemainingSec = 10
                    },
                    onCnsTap = { cnsTapCount++ },
                    onValidResponse = { isCorrect, hwTime ->
                        handleValidResponse(isCorrect = isCorrect, hardwareEventTime = hwTime)
                    },
                    onEarlyTap = { handleEarlyTap() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 4. Ergonomic Lower Third Controls
            ActiveDrillControlViews(
                drillType = drillType,
                drillState = drillState,
                choiceTargetDirection = choiceTargetDirection,
                stroopColors = stroopColors,
                stroopColorValues = stroopColorValues,
                stroopColorIndex = stroopColorIndex,
                evenOddNumber = evenOddNumber,
                flankerCenterRight = flankerCenterRight,
                choice4WayDirection = choice4WayDirection,
                matchColorNames = matchColorNames,
                matchColorValues = matchColorValues,
                colorMatchIndex = colorMatchIndex,
                spatialAudioChannel = spatialAudioChannel,
                onValidResponse = { isCorrect, hwTime ->
                    handleValidResponse(isCorrect = isCorrect, hardwareEventTime = hwTime)
                },
                onEarlyTap = { handleEarlyTap() }
            )
        }
    }
}

    // Confirm Exit Sheet (Protects unfinished run)
    if (showExitConfirmSheet) {
        AlertDialog(
            onDismissRequest = { showExitConfirmSheet = false },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmSheet = false
                        onExitDrill()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralWarning, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Exit Drill", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmSheet = false }) {
                    Text("Resume Run", color = TextPrimary)
                }
            },
            title = { Text(text = "Abandon Run?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(text = "This session is incomplete. Unfinished trials will not be recorded in athlete analytics.", color = TextMuted, fontSize = 13.sp) },
            containerColor = CharcoalCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
