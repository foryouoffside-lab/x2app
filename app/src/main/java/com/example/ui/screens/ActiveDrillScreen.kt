package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillType
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CoolBlue
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class DrillState {
    STANDBY,
    WAITING_FOR_STIMULUS,
    STIMULUS_ACTIVE,
    TOO_SOON,
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
    onCompleteRun: (medianMs: Long, bestMs: Long, accuracyPercent: Int, consistencyMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalTrials = 5
    var currentTrial by remember { mutableIntStateOf(1) }
    var drillState by remember { mutableStateOf(DrillState.STANDBY) }
    var stimulusStartTime by remember { mutableLongStateOf(0L) }
    var lastFeedbackMs by remember { mutableLongStateOf(0L) }
    var showExitConfirmSheet by remember { mutableStateOf(false) }

    // Recorded trial metrics
    val recordedTimes = remember { mutableListOf<Long>() }
    var correctCount by remember { mutableIntStateOf(0) }
    var errorCount by remember { mutableIntStateOf(0) }

    // Choice reaction state (0 = Left, 1 = Right)
    var choiceTargetDirection by remember { mutableIntStateOf(0) }

    // Flash grid active index (0 to 8 for 3x3)
    var activeGridIndex by remember { mutableIntStateOf(-1) }

    // Precision target offset (fraction 0f to 1f)
    var precisionTargetX by remember { mutableStateOf(0.5f) }
    var precisionTargetY by remember { mutableStateOf(0.4f) }

    fun triggerHaptic() {
        if (!hapticsEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    fun finishRun() {
        drillState = DrillState.FINISHED
        val times = if (recordedTimes.isEmpty()) listOf(240L) else recordedTimes
        val sorted = times.sorted()
        val median = sorted[sorted.size / 2]
        val best = sorted.first()
        val totalAttempts = (correctCount + errorCount).coerceAtLeast(1)
        val accuracy = ((correctCount.toFloat() / totalAttempts.toFloat()) * 100).toInt().coerceIn(50, 100)

        // Consistency calculation (variance/std dev approximation)
        val mean = times.average()
        val variance = times.map { (it - mean) * (it - mean) }.average()
        val consistency = kotlin.math.sqrt(variance).toLong().coerceIn(8, 45)

        onCompleteRun(median, best, accuracy, consistency)
    }

    fun startNextTrial() {
        if (currentTrial > totalTrials) {
            finishRun()
            return
        }
        drillState = DrillState.WAITING_FOR_STIMULUS
    }

    // Coroutine for stimulus trigger delay
    LaunchedEffect(currentTrial, drillState) {
        if (drillState == DrillState.WAITING_FOR_STIMULUS) {
            // Random delay between 1.5s and 3.8s
            val delayMs = Random.nextLong(1500L, 3800L)
            delay(delayMs)
            if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                // Trigger stimulus
                when (drillType) {
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
                }
                stimulusStartTime = System.currentTimeMillis()
                drillState = DrillState.STIMULUS_ACTIVE
                triggerHaptic()
            }
        }
    }

    // Auto-advance to trial 1 on first render
    LaunchedEffect(Unit) {
        delay(400)
        startNextTrial()
    }

    fun handleEarlyTap() {
        if (drillState == DrillState.WAITING_FOR_STIMULUS) {
            errorCount++
            drillState = DrillState.TOO_SOON
            triggerHaptic()
        }
    }

    fun handleValidResponse(isCorrect: Boolean = true) {
        if (drillState != DrillState.STIMULUS_ACTIVE) return
        val now = System.currentTimeMillis()
        val elapsed = (now - stimulusStartTime).coerceAtLeast(120L)

        triggerHaptic()
        lastFeedbackMs = elapsed
        recordedTimes.add(elapsed)
        if (isCorrect) correctCount++ else errorCount++

        drillState = DrillState.TRIAL_FEEDBACK
    }

    // Background color based on drill state
    val stimulusBgColor = when (drillState) {
        DrillState.STIMULUS_ACTIVE -> {
            if (drillType == DrillType.CLASSIC) ElectricLime else DarkBackground
        }
        DrillState.TOO_SOON -> CoralWarning.copy(alpha = 0.2f)
        else -> DarkBackground
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(stimulusBgColor)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Header Row: Exit (X), Title / Verified run, Pause
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

                Text(
                    text = if (isDailyMode) "Verified run" else drillType.title,
                    color = if (isDailyMode) ElectricLime else TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isDailyMode) {
                    Box(modifier = Modifier.size(44.dp)) // Pause disabled in ranked/daily mode
                } else {
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

            // 2. Status row: Trial X / 5 and segmented progress indicator
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trial $currentTrial / $totalTrials",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (lastFeedbackMs > 0) {
                        Text(
                            text = "Last: $lastFeedbackMs ms",
                            color = ElectricLime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                                        isDone -> ElectricLime
                                        isCurrent -> ElectricLime.copy(alpha = 0.6f)
                                        else -> BorderSubtle
                                    }
                                )
                        )
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
                        .background(AmberAlert.copy(alpha = 0.2f))
                        .border(1.dp, AmberAlert, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Too soon - reset and try again.",
                        color = AmberAlert,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 3. Full-screen stimulus zone
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable {
                        if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                            handleEarlyTap()
                        } else if (drillState == DrillState.STIMULUS_ACTIVE && drillType == DrillType.CLASSIC) {
                            handleValidResponse()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                when (drillType) {
                    DrillType.CLASSIC -> {
                        // Full screen visual response
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
                                    Text("Wait for Electric Lime flash...", color = TextMuted, fontSize = 14.sp)
                                }
                                DrillState.STIMULUS_ACTIVE -> {
                                    Text(
                                        text = "TAP NOW!",
                                        color = DarkBackground,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                DrillState.TOO_SOON -> {
                                    Button(
                                        onClick = { startNextTrial() },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Reset & Continue", fontWeight = FontWeight.Bold)
                                    }
                                }
                                DrillState.TRIAL_FEEDBACK -> {
                                    Text(
                                        text = "$lastFeedbackMs ms",
                                        color = ElectricLime,
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            currentTrial++
                                            startNextTrial()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = DarkBackground),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Next Trial", fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }

                    DrillType.CHOICE -> {
                        // Choice Direction stimulus
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                Text("Prepare for directional cue...", color = TextMuted, fontSize = 14.sp)
                            } else if (drillState == DrillState.STIMULUS_ACTIVE) {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .background(CharcoalCardElevated, CircleShape)
                                        .border(2.dp, ElectricLime, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (choiceTargetDirection == 0) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = ElectricLime,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (choiceTargetDirection == 0) "TAP LEFT" else "TAP RIGHT",
                                    color = ElectricLime,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            } else if (drillState == DrillState.TRIAL_FEEDBACK) {
                                Text(
                                    text = "$lastFeedbackMs ms",
                                    color = ElectricLime,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        currentTrial++
                                        startNextTrial()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = DarkBackground),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Next Trial", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    DrillType.PRECISION -> {
                        // Dynamic Target Disc in 2D space
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
                                        .background(ElectricLime)
                                        .border(3.dp, DarkBackground, CircleShape)
                                        .clickable { handleValidResponse() },
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
                                    Text("Watch for target disc...", color = TextMuted, fontSize = 14.sp)
                                }
                            } else if (drillState == DrillState.TRIAL_FEEDBACK) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$lastFeedbackMs ms", color = ElectricLime, fontSize = 40.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                currentTrial++
                                                startNextTrial()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = DarkBackground),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Next Target", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    DrillType.FLASH_GRID -> {
                        // 3x3 Matrix Grid
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (drillState == DrillState.TRIAL_FEEDBACK) {
                                Text("$lastFeedbackMs ms", color = ElectricLime, fontSize = 36.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        currentTrial++
                                        startNextTrial()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = DarkBackground),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Next Pulse", fontWeight = FontWeight.Bold)
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
                                                .background(if (isFlashing) ElectricLime else CharcoalCard)
                                                .border(1.dp, if (isFlashing) ElectricLime else BorderSubtle, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                        handleEarlyTap()
                                                    } else if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                        if (isFlashing) {
                                                            handleValidResponse(true)
                                                        } else {
                                                            handleValidResponse(false)
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

            // 4. Ergonomic lower third controls (Min 48dp touch targets)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                when (drillType) {
                    DrillType.CHOICE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left response button
                            Button(
                                onClick = {
                                    if (drillState == DrillState.STIMULUS_ACTIVE) {
                                        handleValidResponse(choiceTargetDirection == 0)
                                    } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                        handleEarlyTap()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated, contentColor = TextPrimary),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(68.dp)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                    .testTag("choice_left_target")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("LEFT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            // Right response button
                            Button(
                                onClick = {
                                    if (drillState == DrillState.STIMULUS_ACTIVE) {
                                        handleValidResponse(choiceTargetDirection == 1)
                                    } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                        handleEarlyTap()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CharcoalCardElevated, contentColor = TextPrimary),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(68.dp)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                    .testTag("choice_right_target")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("RIGHT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }

                    DrillType.CLASSIC -> {
                        Button(
                            onClick = {
                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                    handleValidResponse()
                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                    handleEarlyTap()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (drillState == DrillState.STIMULUS_ACTIVE) ElectricLime else CharcoalCardElevated,
                                contentColor = if (drillState == DrillState.STIMULUS_ACTIVE) DarkBackground else TextPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .testTag("classic_lower_target")
                        ) {
                            Text(
                                text = if (drillState == DrillState.STIMULUS_ACTIVE) "TAP TRIGGER!" else "RESPONSE ZONE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    else -> {
                        // Informational hint
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CharcoalCard, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Distraction-free reflex capture active",
                                color = TextSubtle,
                                fontSize = 12.sp
                            )
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
            text = { Text(text = "This session is incomplete. Unfinished trials will not be recorded in your RPI index.", color = TextMuted, fontSize = 13.sp) },
            containerColor = CharcoalCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
