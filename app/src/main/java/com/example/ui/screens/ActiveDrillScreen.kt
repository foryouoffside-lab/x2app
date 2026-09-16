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
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillState
import com.example.model.DrillType
import com.example.model.TrialRecord
import com.example.model.DrillMode
import com.example.model.TrainingRules
import com.example.model.supportsTrainMode
import com.example.model.warmUpTrialsCount
import com.example.model.catchTrialRate
import com.example.model.responseDeadlineMs
import com.example.model.scoredTrialsCount
import com.example.model.totalTrialsCount
import com.example.model.trainTuning
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
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun ActiveDrillScreen(
    drillType: DrillType,
    isDailyMode: Boolean,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    mode: DrillMode = DrillMode.TEST,
    // Systematic input/display overhead for this device, from calibration. Subtracted
    // from every measurement by shifting the stimulus zero point forward.
    displayLatencyMs: Long = 0L,
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
        rawTrials: List<TrialRecord>,
        mode: DrillMode,
        survivedSec: Int,
        levelReached: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localView = LocalView.current
    val refreshRateHz = remember { localView.display?.refreshRate?.roundToInt()?.takeIf { it > 0 } ?: 60 }
    val totalTrials = drillType.totalTrialsCount

    // Train mode: the clock is the only fail state. Difficulty keys off level, never
    // off elapsed time - with a refillable clock a time-based ramp runs backwards.
    val isTrain = mode == DrillMode.TRAIN && drillType.supportsTrainMode
    val tuning = remember(drillType) { drillType.trainTuning }
    var trainClockSec by remember { mutableFloatStateOf(TrainingRules.TOTAL_TIME_SEC) }
    var trainHits by remember { mutableIntStateOf(0) }
    var trainLevel by remember { mutableIntStateOf(1) }
    var trainElapsedSec by remember { mutableIntStateOf(0) }


    // Train scoring: a hit buys time, a mistake costs it, and level follows hits.
    fun registerTrainOutcome(isCorrect: Boolean) {
        if (!isTrain) return
        if (isCorrect) {
            trainHits++
            trainLevel = TrainingRules.levelForHits(trainHits, tuning.hitsPerLevel)
            trainClockSec = TrainingRules.applyHit(trainClockSec, tuning.timePerHitSec)
        } else {
            trainClockSec = TrainingRules.applyMistake(trainClockSec)
        }
    }


    var currentTrial by remember { mutableIntStateOf(1) }
    var drillState by remember { mutableStateOf(DrillState.STANDBY) }
    // Test protocol structure. Train has no warm-up or catch trials: it is a game
    // against a clock, not a measurement.
    val warmUpTrials = if (isTrain) 0 else drillType.warmUpTrialsCount
    val isWarmUpTrial = !isTrain && currentTrial <= warmUpTrials
    // Which trial indices carry no stimulus at all, decided up front so the count is exact.
    val catchTrialIndices = remember(drillType, isTrain) {
        if (isTrain || drillType.catchTrialRate <= 0f) emptySet()
        else {
            val scored = drillType.scoredTrialsCount + drillType.warmUpTrialsCount
            val howMany = kotlin.math.ceil(scored * drillType.catchTrialRate).toInt()
            // Never in the warm-up, and never the very first scored trial.
            ((warmUpTrials + 2)..drillType.totalTrialsCount).shuffled().take(howMany).toSet()
        }
    }
    val isCatchTrial = currentTrial in catchTrialIndices
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
            // Mean inter-tap interval. The tap test measures a rate, not individual
            // reactions, so there is no separate "best" trial to report.
            val medianMs = if (freqHz > 0) (1000f / freqHz).toLong() else 0L
            val bestMs = medianMs
            val consistency = kotlin.math.abs(cnsFirstHalfTaps - cnsSecondHalfTaps).toLong()
            val cv = if (totalTaps > 0) (consistency.toFloat() / totalTaps.toFloat()) * 100f else 0f

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
                ),
                DrillMode.TEST,
                0,
                0
            )
            return
        }

        // Statistics are computed from scored trials only: practice trials carry a
        // first-trial slowing that would bias the median, and catch trials have no
        // stimulus so they have no latency to contribute.
        val scoredTrials = recordedTrials.filter { !it.isWarmUp && !it.isCatchTrial }
        val validTimes = scoredTrials.filter { it.isCorrect && !it.isFalseStart }.map { it.latencyMs }
        // A response on a catch trial is a pure false alarm - the strongest evidence of
        // guessing the app can collect.
        val falseAlarms = recordedTrials.count { it.isCatchTrial && !it.isCorrect }
        // A run with no scoring trials has nothing to measure. Report zeros rather than
        // inventing a placeholder time, so an empty run never looks like a real result.
        val times = validTimes
        val sorted = times.sorted()
        // Which estimator is right depends on how many trials there are.
        //
        // Test runs 5 trials. At that size the median is the right call: a trimmed mean
        // would have to discard the fastest valid trial, and in a right-skewed reaction
        // time distribution the fast trials are signal while the slow tail is the noise.
        //
        // Train produces 25-40 trials. At that size a symmetric 10% trimmed mean uses far
        // more of the data than a single middle value while still discarding lapses at
        // both ends, so it is both more stable and more precise than the median.
        val median = when {
            sorted.isEmpty() -> 0L
            sorted.size < 12 -> sorted[sorted.size / 2]
            else -> {
                val cut = (sorted.size * 0.1).toInt().coerceAtLeast(1)
                val kept = sorted.subList(cut, sorted.size - cut)
                if (kept.isEmpty()) sorted[sorted.size / 2] else kept.average().toLong()
            }
        }
        val best = sorted.firstOrNull() ?: 0L
        val scoredCorrect = scoredTrials.count { it.isCorrect }
        val totalAttempts = scoredTrials.size.coerceAtLeast(1)
        val accuracy = ((scoredCorrect.toFloat() / totalAttempts.toFloat()) * 100).toInt().coerceIn(0, 100)

        // Standard deviation and Coefficient of Variation (CV% = StdDev / Mean * 100),
        // reported as measured rather than squeezed into a flattering range.
        val mean = if (times.isEmpty()) 0.0 else times.average()
        val variance = if (times.isEmpty()) 0.0 else times.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        val consistency = stdDev.toLong()
        val cvPercent = if (mean > 0) ((stdDev / mean) * 100f).toFloat() else 0f

        // Inverse Efficiency Score: IES = RT / (Accuracy / 100)
        val accRatio = (accuracy.toFloat() / 100f).coerceAtLeast(0.3f)
        val iesScore = (median / accRatio).toLong()

        // Ex-Gaussian Tau approximation: Attentional lapse tail (90th percentile - median)
        val exTau = if (sorted.isEmpty()) 0L else {
            val p90Index = (sorted.size * 0.9).toInt().coerceIn(0, sorted.size - 1)
            (sorted[p90Index] - median).coerceAtLeast(0L)
        }

        onCompleteRun(
            median,
            best,
            accuracy,
            consistency,
            (cvPercent * 10f).roundToInt() / 10f,
            falseStartsCount + falseAlarms,
            iesScore,
            exTau,
            null,
            recordedTrials.toList(),
            if (isTrain) DrillMode.TRAIN else DrillMode.TEST,
            if (isTrain) trainElapsedSec else 0,
            if (isTrain) trainLevel else 0
        )
    }

    fun startNextTrial() {
        if (drillType == DrillType.CNS_TAP) {
            isCnsRunning = true
            return
        }
        // Test ends after a fixed number of trials so every run is comparable.
        // Train runs until the clock is gone, however many trials that takes.
        if (!isTrain && currentTrial > totalTrials) {
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
                stimulusStartTime = 0L // set on the frame that actually presents it
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
            // Test keeps the standard 1.6-3.9s psychometric jitter: the foreperiod is part
            // of the instrument and must not vary, or measurements stop being comparable.
            // Train paces stimuli to the drill's action rate so the clock maths hold, and
            // tightens the gap as level rises.
            // Catch trial: no stimulus ever arrives. Hold for a full foreperiod plus a
            // response window; surviving it without tapping is a correct rejection.
            if (isCatchTrial) {
                delay(nonAgingForeperiodMs())
                delay(1200L)
                if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                    correctCount++
                    lastFeedbackMs = 0L
                    lastFeedbackMsg = "Held - no signal"
                    recordedTrials.add(
                        TrialRecord(
                            trialIndex = currentTrial,
                            latencyMs = 0L,
                            isCorrect = true,
                            isFalseStart = false,
                            stimulusInfo = "Catch trial - correctly withheld",
                            isWarmUp = isWarmUpTrial,
                            isCatchTrial = true
                        )
                    )
                    drillState = DrillState.TRIAL_FEEDBACK
                }
                return@LaunchedEffect
            }

            val delayMs = if (isTrain) {
                val baseMs = (1000f / tuning.actionsPerSec)
                val tightened = baseMs * TrainingRules.rampUp(trainLevel, 1.0f, 0.55f)
                val jitter = tightened * 0.35f
                Random.nextLong(
                    (tightened - jitter).toLong().coerceAtLeast(350L),
                    (tightened + jitter).toLong().coerceAtLeast(600L)
                )
            } else {
                nonAgingForeperiodMs()
            }
            delay(delayMs)
            if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                when (drillType) {
                    DrillType.GO_NO_GO -> {
                        // ~65% Go, ~35% No-Go
                        // 75/25 builds a dominant Go response for the athlete to inhibit against.
                        // At 65/35 there is no prepotency and this measures choice, not inhibition.
                        isGoStimulus = Random.nextFloat() < 0.75f
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
                stimulusStartTime = 0L // set on the frame that actually presents it
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
                                stimulusInfo = "NO-GO (Red Octagon) - Successfully Withheld",
                                isWarmUp = isWarmUpTrial
                            )
                        )
                        registerTrainOutcome(true)
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

    // Train is played against a clock, so it advances itself; stopping for a "Next
    // Trial" tap after every stimulus would break the pressure the mode exists to create.
    LaunchedEffect(isTrain, drillState, currentTrial, trainHits) {
        if (!isTrain) return@LaunchedEffect
        if (drillState != DrillState.TRIAL_FEEDBACK &&
            drillState != DrillState.TOO_SOON &&
            drillState != DrillState.FALSE_START
        ) return@LaunchedEffect
        // Long enough to read the result, short enough not to feel like waiting.
        delay(300L)
        if (drillState == DrillState.TRIAL_FEEDBACK ||
            drillState == DrillState.TOO_SOON ||
            drillState == DrillState.FALSE_START
        ) {
            currentTrial++
            startNextTrial()
        }
    }

    // The measurement's zero point.
    //
    // Setting the clock when the state variable changes measures from before the athlete
    // could possibly see anything: Compose still has to recompose and the display still
    // has to scan the frame out, which is 1-3 frames of error on every single trial.
    // withFrameNanos returns the vsync timestamp of the frame that actually carries the
    // stimulus, on the same uptime base as MotionEvent times, so response - stimulus is
    // a like-for-like subtraction. What remains after vsync is panel latency, which is
    // what the calibration offset accounts for.
    LaunchedEffect(drillState, currentTrial, trainHits) {
        if (drillState != DrillState.STIMULUS_ACTIVE) return@LaunchedEffect
        // Rhythm Sync aims at a future target time it sets itself.
        if (drillType == DrillType.RHYTHM_SYNC || drillType == DrillType.CNS_TAP) return@LaunchedEffect
        if (stimulusStartTime != 0L) return@LaunchedEffect
        withFrameNanos { frameTimeNanos ->
            stimulusStartTime = (frameTimeNanos / 1_000_000L) + displayLatencyMs
        }
    }

    // Train difficulty ramp. The response window closes as level rises; letting it
    // expire costs clock time exactly like a wrong answer. This is what makes levelling
    // mean something - without it the level number would be decoration.
    LaunchedEffect(isTrain, drillState, currentTrial, trainHits) {
        if (drillState != DrillState.STIMULUS_ACTIVE) return@LaunchedEffect
        // No-Go stimuli are scored by withholding, which has its own timer.
        if (drillType == DrillType.GO_NO_GO && !isGoStimulus) return@LaunchedEffect
        if (drillType == DrillType.RHYTHM_SYNC || drillType == DrillType.CNS_TAP) return@LaunchedEffect

        // Train closes the window as level rises; Test uses a fixed, generous deadline so
        // a lapse is scored as a miss instead of entering the median as a huge latency.
        val windowMs = if (isTrain) {
            tuning.windowMsForLevel(trainLevel)
        } else {
            drillType.responseDeadlineMs.takeIf { it > 0L } ?: return@LaunchedEffect
        }
        delay(windowMs)
        if (drillState == DrillState.STIMULUS_ACTIVE) {
            errorCount++
            lastFeedbackMs = 0L
            lastFeedbackMsg = "Missed window"
            recordedTrials.add(
                TrialRecord(
                    trialIndex = currentTrial,
                    latencyMs = windowMs,
                    isCorrect = false,
                    isFalseStart = false,
                    stimulusInfo = if (isTrain) "Window expired at level $trainLevel" else "No response within ${windowMs}ms",
                    isWarmUp = isWarmUpTrial
                )
            )
            registerTrainOutcome(false)
            drillState = DrillState.TRIAL_FEEDBACK
        }
    }

    // Train clock. Drains in real time and is the only fail state; hits refill it.
    LaunchedEffect(isTrain, isTutorialActive) {
        if (!isTrain || isTutorialActive) return@LaunchedEffect
        while (trainClockSec > 0f && drillState != DrillState.FINISHED) {
            delay(100L)
            if (drillState == DrillState.PAUSED) continue
            trainClockSec = (trainClockSec - 0.1f).coerceAtLeast(0f)
            trainElapsedSec = ((TrainingRules.TOTAL_TIME_SEC - trainClockSec) + trainHits * tuning.timePerHitSec).toInt()
        }
        if (drillState != DrillState.FINISHED) finishRun()
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
            registerTrainOutcome(false)
            drillState = DrillState.TOO_SOON
            triggerHaptic(true)
        }
    }

    fun handleValidResponse(isCorrect: Boolean = true, hardwareEventTime: Long? = null, overrideLatency: Long? = null) {
        if (drillState != DrillState.STIMULUS_ACTIVE) return
        // The stimulus frame has not been presented yet, so there is no zero point to
        // measure from. Treat this as an early tap rather than inventing a latency.
        if (stimulusStartTime == 0L && overrideLatency == null) {
            handleEarlyTap()
            return
        }
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
                    stimulusInfo = "Anticipation Violation (<100ms Olympic Standard)",
                    isWarmUp = isWarmUpTrial
                )
            )
            registerTrainOutcome(false)
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
                    stimulusInfo = "NO-GO (Red Octagon) - Failed to Inhibit",
                    isWarmUp = isWarmUpTrial
                )
            )
            registerTrainOutcome(false)
            drillState = DrillState.TRIAL_FEEDBACK
            return
        }

        lastFeedbackMs = elapsed
        // A response to the wrong target is an error and has to read as one. It was
        // already being scored as incorrect, but showing the bare time made a miss
        // look identical to a hit.
        lastFeedbackMsg = if (isCorrect) "$elapsed ms" else "Wrong choice · $elapsed ms"
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
                stimulusInfo = stimInfo,
                isWarmUp = isWarmUpTrial
            )
        )

        registerTrainOutcome(isCorrect)
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
                            // Never assert a refresh rate: read the real one, and say
                            // plainly whether a timing calibration is actually applied.
                            text = if (displayLatencyMs > 0L) {
                                "$refreshRateHz Hz · ${displayLatencyMs}ms calibrated"
                            } else {
                                "$refreshRateHz Hz · uncalibrated"
                            },
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
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                // The clock turns amber once it is short enough to lose on.
                                tint = if (isTrain && trainClockSec <= 10f) AmberAlert else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when {
                                    isTrain -> "${ceil(trainClockSec).toInt()}s  ·  LVL $trainLevel"
                                    // Say so plainly: an athlete who thinks practice counts
                                    // will tighten up and skew their own first trials.
                                    isWarmUpTrial -> "Warm-up $currentTrial / $warmUpTrials · not scored"
                                    else -> "Trial ${currentTrial - warmUpTrials} / ${drillType.scoredTrialsCount}"
                                },
                                color = if (isTrain && trainClockSec <= 10f) AmberAlert else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (lastFeedbackMs > 0 || lastFeedbackMsg.isNotEmpty()) {
                            // One shared predicate, so a new failure message can never be
                            // styled as a success in one place and a failure in another.
                            val feedbackColor = if (isFailureFeedback(lastFeedbackMsg)) CoralWarning else SportGreen
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = feedbackColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    // Glyph as well as colour: the outcome must be legible
                                    // to an athlete with red-green colour blindness.
                                    text = outcomeGlyph(lastFeedbackMsg) + " " + lastFeedbackMsg.ifEmpty { "$lastFeedbackMs ms" },
                                    color = feedbackColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Train shows the clock draining; Test shows fixed trial segments.
                    if (isTrain) {
                        val clockFraction = (trainClockSec / TrainingRules.TOTAL_TIME_SEC).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(BorderSubtle)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(clockFraction)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (trainClockSec <= 10f) AmberAlert else BrandAccent)
                            )
                        }
                    } else Row(
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

/**
 * A foreperiod whose hazard rate does not rise as you wait.
 *
 * A uniform delay is predictable: the longer nothing has happened, the sooner it must,
 * so athletes learn to time the late trials and anticipation contaminates the measurement.
 * An exponential (non-aging) distribution has constant hazard - having waited two seconds
 * tells you nothing about the next instant - which is why it is the standard choice for
 * reaction-time protocols. Truncated so a trial cannot run absurdly long.
 */
private fun nonAgingForeperiodMs(
    minMs: Long = 1200L,
    meanMs: Long = 1400L,
    maxMs: Long = 5000L
): Long {
    val u = Random.nextDouble().coerceIn(1e-9, 1.0)
    val exponential = (-kotlin.math.ln(u) * meanMs).toLong()
    return (minMs + exponential).coerceAtMost(maxMs)
}
