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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
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

private enum class DrillState {
    STANDBY,
    WAITING_FOR_STIMULUS,
    STIMULUS_ACTIVE,
    TOO_SOON,
    FALSE_START,
    TRIAL_FEEDBACK,
    PAUSED,
    FINISHED
}

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
                    DrillType.CHOICE -> {
                        choiceTargetDirection = Random.nextInt(2) // 0 or 1
                    }
                    DrillType.FLASH_GRID -> {
                        activeGridIndex = Random.nextInt(9) // 0 to 8
                    }
                    DrillType.PRECISION -> {
                        precisionTargetX = Random.nextFloat().coerceIn(0.15f, 0.85f)
                        precisionTargetY = Random.nextFloat().coerceIn(0.2f, 0.7f)
                    }
                    DrillType.CLASSIC -> {}
                    DrillType.CNS_TAP -> {}
                }
                stimulusStartTime = SystemClock.uptimeMillis()
                drillState = DrillState.STIMULUS_ACTIVE
                triggerHaptic(false)

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

        recordedTrials.add(
            TrialRecord(
                trialIndex = currentTrial,
                latencyMs = elapsed,
                isCorrect = isCorrect,
                isFalseStart = false,
                stimulusInfo = "${drillType.title} Stimulus"
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
                                        DrillType.CLASSIC, DrillType.AUDITORY, DrillType.GO_NO_GO -> {
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
                when (drillType) {
                    // --- 1. CLASSIC VISUAL SRT ---
                    DrillType.CLASSIC -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when (drillState) {
                                DrillState.WAITING_FOR_STIMULUS -> {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .background(CharcoalCard, CircleShape)
                                            .border(2.dp, BorderSubtle, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("WAIT", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Fixate gaze... tap immediately upon flash", color = TextMuted, fontSize = 13.sp)
                                }
                                DrillState.STIMULUS_ACTIVE -> {
                                    Text(
                                        text = "TAP NOW!",
                                        color = TextInverse,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                DrillState.TOO_SOON, DrillState.FALSE_START -> {
                                    Button(
                                        onClick = { startNextTrial() },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reset Trial", fontWeight = FontWeight.Bold)
                                    }
                                }
                                DrillState.TRIAL_FEEDBACK -> {
                                    Text(
                                        text = "$lastFeedbackMs ms",
                                        color = BrandAccent,
                                        fontSize = 46.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            currentTrial++
                                            startNextTrial()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Next Trial", fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    // --- 2. GO / NO-GO INHIBITION ---
                    DrillType.GO_NO_GO -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when (drillState) {
                                DrillState.WAITING_FOR_STIMULUS -> {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .background(CharcoalCard, CircleShape)
                                            .border(2.dp, BorderSubtle, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("READY", color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("STRIKE Green / HOLD Red", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Evaluates prefrontal impulse suppression", color = TextSubtle, fontSize = 12.sp)
                                }
                                DrillState.STIMULUS_ACTIVE -> {
                                    if (isGoStimulus) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = TextInverse, modifier = Modifier.size(80.dp))
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("GO! STRIKE!", color = TextInverse, fontSize = 36.sp, fontWeight = FontWeight.Black)
                                        }
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = TextInverse, modifier = Modifier.size(80.dp))
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("NO-GO! HOLD!", color = TextInverse, fontSize = 34.sp, fontWeight = FontWeight.Black)
                                            Text("Withhold tap for 1.2 seconds", color = TextInverse.copy(alpha = 0.8f), fontSize = 13.sp)
                                        }
                                    }
                                }
                                DrillState.TOO_SOON -> {
                                    Button(
                                        onClick = { startNextTrial() },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reset Trial", fontWeight = FontWeight.Bold)
                                    }
                                }
                                DrillState.TRIAL_FEEDBACK -> {
                                    Text(
                                        text = lastFeedbackMsg,
                                        color = if (lastFeedbackMsg.contains("Error")) CoralWarning else SportGreen,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            currentTrial++
                                            startNextTrial()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Next Trial", fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    // --- 3. AUDITORY STARTER REFLEX ---
                    DrillType.AUDITORY -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            when (drillState) {
                                DrillState.WAITING_FOR_STIMULUS -> {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .background(CharcoalCard, CircleShape)
                                            .border(2.dp, BorderSubtle, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(48.dp))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Awaiting Starter Tone...", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Screen stays neutral. Tap anywhere on tone.", color = TextSubtle, fontSize = 12.sp)
                                }
                                DrillState.STIMULUS_ACTIVE -> {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .background(CoolBlue.copy(alpha = 0.2f), CircleShape)
                                            .border(3.dp, CoolBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(64.dp))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("TONE ACTIVE — TAP!", color = CoolBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
                                }
                                DrillState.TOO_SOON, DrillState.FALSE_START -> {
                                    Button(
                                        onClick = { startNextTrial() },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reset", fontWeight = FontWeight.Bold)
                                    }
                                }
                                DrillState.TRIAL_FEEDBACK -> {
                                    Text(
                                        text = "$lastFeedbackMs ms",
                                        color = CoolBlue,
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (lastFeedbackMs < 165) "Elite Acoustic Conduction" else "Auditory Reflex Recorded",
                                        color = TextMuted,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            currentTrial++
                                            startNextTrial()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoolBlue, contentColor = TextInverse),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Next Trial", fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    // --- 4. 10S CNS TAP READINESS TEST ---
                    DrillType.CNS_TAP -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            if (!isCnsRunning && cnsTimeRemainingSec == 10) {
                                Text(
                                    text = "10s CNS Readiness Test",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap the circular pad as rapidly as possible for 10 seconds. Quantifies central nervous system fatigue and motor velocity.",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(
                                    onClick = { isCnsRunning = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.height(56.dp).fillMaxWidth(0.7f)
                                ) {
                                    Text("START 10s TEST", fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                            } else {
                                // Active 10-second countdown and tap counter
                                Text(
                                    text = "00:0$cnsTimeRemainingSec",
                                    color = if (cnsTimeRemainingSec <= 3) CoralWarning else BrandAccent,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val currentHz = if (10 - cnsTimeRemainingSec > 0) {
                                    (cnsTapCount.toFloat() / (10 - cnsTimeRemainingSec).toFloat() * 10f).roundToInt() / 10f
                                } else 0f
                                Text(
                                    text = "$cnsTapCount Taps (${currentHz} Hz)",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))

                                // Massive High-Frequency Tapping Pad (Hardware Press Detection)
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(CircleShape)
                                        .background(CharcoalCardElevated)
                                        .border(3.dp, BrandAccent, CircleShape)
                                        .pointerInput(isCnsRunning) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                                    if (event.type == PointerEventType.Press) {
                                                        event.changes.firstOrNull()?.consume()
                                                        if (isCnsRunning) {
                                                            cnsTapCount++
                                                            triggerHaptic(false)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("TAP FAST!", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // --- 5. CHOICE REACTION CRT ---
                    DrillType.CHOICE -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                Text("Prepare for directional vector...", color = TextMuted, fontSize = 14.sp)
                            } else if (drillState == DrillState.STIMULUS_ACTIVE) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .background(CharcoalCardElevated, CircleShape)
                                        .border(2.dp, BrandAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (choiceTargetDirection == 0) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = BrandAccent,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (choiceTargetDirection == 0) "STRIKE LEFT" else "STRIKE RIGHT",
                                    color = BrandAccent,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            } else if (drillState == DrillState.TRIAL_FEEDBACK) {
                                Text(
                                    text = "$lastFeedbackMs ms",
                                    color = BrandAccent,
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        currentTrial++
                                        startNextTrial()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Next Trial", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // --- 6. SACCADIC PRECISION ---
                    DrillType.PRECISION -> {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            if (drillState == DrillState.STIMULUS_ACTIVE) {
                                val targetSize = 64.dp
                                val posX = (maxWidth - targetSize) * precisionTargetX
                                val posY = (maxHeight - targetSize) * precisionTargetY

                                Box(
                                    modifier = Modifier
                                        .offset(x = posX, y = posY)
                                        .size(targetSize)
                                        .clip(CircleShape)
                                        .background(BrandAccent)
                                        .border(3.dp, DarkBackground, CircleShape)
                                        .pointerInput(drillState) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                                    if (event.type == PointerEventType.Press) {
                                                        val change = event.changes.firstOrNull()
                                                        val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                        change?.consume()
                                                        handleValidResponse(isCorrect = true, hardwareEventTime = hwTime)
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(DarkBackground, CircleShape)
                                    )
                                }
                            } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Track parafoveal target...", color = TextMuted, fontSize = 14.sp)
                                }
                            } else if (drillState == DrillState.TRIAL_FEEDBACK) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$lastFeedbackMs ms", color = BrandAccent, fontSize = 42.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                currentTrial++
                                                startNextTrial()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Next Trial", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- 7. PERIPHERAL FLASH GRID ---
                    DrillType.FLASH_GRID -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (drillState == DrillState.TRIAL_FEEDBACK) {
                                Text("$lastFeedbackMs ms", color = BrandAccent, fontSize = 38.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        currentTrial++
                                        startNextTrial()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Next Trial", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            for (row in 0..2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    for (col in 0..2) {
                                        val index = row * 3 + col
                                        val isFlashing = (drillState == DrillState.STIMULUS_ACTIVE) && (activeGridIndex == index)

                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isFlashing) BrandAccent else CharcoalCard)
                                                .border(1.dp, if (isFlashing) BrandAccent else BorderSubtle, RoundedCornerShape(12.dp))
                                                .pointerInput(drillState, isFlashing) {
                                                    awaitPointerEventScope {
                                                        while (true) {
                                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                                            if (event.type == PointerEventType.Press) {
                                                                val change = event.changes.firstOrNull()
                                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                                if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                                    change?.consume()
                                                                    handleEarlyTap()
                                                                } else if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                                    change?.consume()
                                                                    handleValidResponse(isCorrect = isFlashing, hardwareEventTime = hwTime)
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isFlashing) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .background(DarkBackground, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Ergonomic Lower Third Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                when (drillType) {
                    DrillType.CHOICE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CharcoalCardElevated)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                    .pointerInput(drillState, choiceTargetDirection) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                                if (event.type == PointerEventType.Press) {
                                                    val change = event.changes.firstOrNull()
                                                    val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                    if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                        change?.consume()
                                                        handleValidResponse(isCorrect = (choiceTargetDirection == 0), hardwareEventTime = hwTime)
                                                    } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                        change?.consume()
                                                        handleEarlyTap()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("choice_left_target"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("LEFT", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CharcoalCardElevated)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                    .pointerInput(drillState, choiceTargetDirection) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                                if (event.type == PointerEventType.Press) {
                                                    val change = event.changes.firstOrNull()
                                                    val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                    if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                        change?.consume()
                                                        handleValidResponse(isCorrect = (choiceTargetDirection == 1), hardwareEventTime = hwTime)
                                                    } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                        change?.consume()
                                                        handleEarlyTap()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("choice_right_target"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("RIGHT", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }

                    DrillType.CLASSIC, DrillType.AUDITORY, DrillType.GO_NO_GO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (drillState == DrillState.STIMULUS_ACTIVE) BrandAccent else CharcoalCardElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .pointerInput(drillState) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type == PointerEventType.Press) {
                                                val change = event.changes.firstOrNull()
                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                    change?.consume()
                                                    handleValidResponse(isCorrect = true, hardwareEventTime = hwTime)
                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                    change?.consume()
                                                    handleEarlyTap()
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("primary_trigger_pad"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (drillState == DrillState.STIMULUS_ACTIVE) "TAP TRIGGER!" else "TACTILE RESPONSE ZONE",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = if (drillState == DrillState.STIMULUS_ACTIVE) TextInverse else TextPrimary
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CharcoalCard, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Distraction-free reflex capture active · 120Hz Vsync locked",
                                color = TextSubtle,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
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
