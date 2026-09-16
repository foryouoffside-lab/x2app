package com.example.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillState
import com.example.model.DrillType
import com.example.ui.theme.*

@Composable
fun ActiveDrillStimulusArea(
    drillType: DrillType,
    drillState: DrillState,
    lastFeedbackMs: Long,
    lastFeedbackMsg: String,
    currentTrial: Int,
    isGoStimulus: Boolean,
    choiceTargetDirection: Int,
    isCnsRunning: Boolean,
    cnsTapCount: Int,
    cnsTimeRemainingSec: Int,
    precisionTargetX: Float,
    precisionTargetY: Float,
    activeGridIndex: Int,
    f1LitCount: Int,
    stroopWord: String,
    stroopColorIndex: Int,
    stroopColorValues: List<Color>,
    evenOddNumber: Int,
    flankerCenterRight: Boolean,
    flankerCongruent: Boolean,
    choice4WayDirection: Int,
    matchColorNames: List<String>,
    matchColorValues: List<Color>,
    colorMatchIndex: Int,
    gridTrackingTargetIndex: Int,
    spatialAudioChannel: Int,
    activeQuadrantIndex: Int,
    onStartNextTrial: () -> Unit,
    onAdvanceTrial: () -> Unit,
    onStartCns: () -> Unit,
    onCnsTap: () -> Unit,
    onValidResponse: (isCorrect: Boolean, hwTime: Long) -> Unit,
    onEarlyTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (drillType) {
            DrillType.CLASSIC, DrillType.AUDITORY, DrillType.TACTILE,
            DrillType.F1_LIGHTS, DrillType.CNS_TAP, DrillType.PRECISION,
            DrillType.FLASH_GRID -> {
                VisualSensoryStimulusView(
                    drillType = drillType,
                    drillState = drillState,
                    lastFeedbackMs = lastFeedbackMs,
                    lastFeedbackMsg = lastFeedbackMsg,
                    f1LitCount = f1LitCount,
                    isCnsRunning = isCnsRunning,
                    cnsTapCount = cnsTapCount,
                    cnsTimeRemainingSec = cnsTimeRemainingSec,
                    precisionTargetX = precisionTargetX,
                    precisionTargetY = precisionTargetY,
                    activeGridIndex = activeGridIndex,
                    onStartNextTrial = onStartNextTrial,
                    onAdvanceTrial = onAdvanceTrial,
                    onStartCns = onStartCns,
                    onCnsTap = onCnsTap,
                    onValidResponse = onValidResponse,
                    onEarlyTap = onEarlyTap
                )
            }
            DrillType.GO_NO_GO, DrillType.CHOICE, DrillType.STROOP,
            DrillType.EVEN_ODD, DrillType.FLANKER, DrillType.RHYTHM_SYNC -> {
                CognitiveStimulusView(
                    drillType = drillType,
                    drillState = drillState,
                    lastFeedbackMs = lastFeedbackMs,
                    lastFeedbackMsg = lastFeedbackMsg,
                    isGoStimulus = isGoStimulus,
                    choiceTargetDirection = choiceTargetDirection,
                    stroopWord = stroopWord,
                    stroopColorIndex = stroopColorIndex,
                    stroopColorValues = stroopColorValues,
                    evenOddNumber = evenOddNumber,
                    flankerCenterRight = flankerCenterRight,
                    flankerCongruent = flankerCongruent,
                    onStartNextTrial = onStartNextTrial,
                    onAdvanceTrial = onAdvanceTrial
                )
            }
            DrillType.CHOICE_4WAY, DrillType.COLOR_MATCH, DrillType.GRID_TRACKING,
            DrillType.SPATIAL_AUDIO, DrillType.QUADRANT_CHOICE -> {
                SpecializedBatteryStimulusView(
                    drillType = drillType,
                    drillState = drillState,
                    lastFeedbackMs = lastFeedbackMs,
                    lastFeedbackMsg = lastFeedbackMsg,
                    choice4WayDirection = choice4WayDirection,
                    matchColorNames = matchColorNames,
                    matchColorValues = matchColorValues,
                    colorMatchIndex = colorMatchIndex,
                    gridTrackingTargetIndex = gridTrackingTargetIndex,
                    spatialAudioChannel = spatialAudioChannel,
                    activeQuadrantIndex = activeQuadrantIndex,
                    onStartNextTrial = onStartNextTrial,
                    onAdvanceTrial = onAdvanceTrial,
                    onValidResponse = onValidResponse,
                    onEarlyTap = onEarlyTap
                )
            }
        }
    }
}

@Composable
private fun VisualSensoryStimulusView(
    drillType: DrillType,
    drillState: DrillState,
    lastFeedbackMs: Long,
    lastFeedbackMsg: String,
    f1LitCount: Int,
    isCnsRunning: Boolean,
    cnsTapCount: Int,
    cnsTimeRemainingSec: Int,
    precisionTargetX: Float,
    precisionTargetY: Float,
    activeGridIndex: Int,
    onStartNextTrial: () -> Unit,
    onAdvanceTrial: () -> Unit,
    onStartCns: () -> Unit,
    onCnsTap: () -> Unit,
    onValidResponse: (isCorrect: Boolean, hwTime: Long) -> Unit,
    onEarlyTap: () -> Unit
) {
    when (drillType) {
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
                        Text("TAP NOW!", color = TextInverse, fontSize = 38.sp, fontWeight = FontWeight.Black)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, CoolBlue), fontSize = 44.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lastFeedbackMs in 1..164) "Elite Acoustic Conduction" else if (lastFeedbackMs <= 0L) "No response" else "Auditory Reflex Recorded",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.TACTILE -> {
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
                            Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(44.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Hold device firmly", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("React instantly upon physical vibration", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(SignalAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(52.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("PULSE ACTIVE · TAP!", color = TextInverse, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.F1_LIGHTS -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..5) {
                                val isLit = (i <= f1LitCount)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .background(CharcoalCardElevated, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isLit) CoralWarning else DarkBackground)
                                            .border(1.dp, if (isLit) CoralWarning else BorderSubtle, CircleShape)
                                    )
                                    Box(modifier = Modifier.size(10.dp).background(DarkBackground, CircleShape))
                                    Box(modifier = Modifier.size(10.dp).background(DarkBackground, CircleShape))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (f1LitCount == 5) "STAND BY FOR LIGHTS OUT..." else "HOLD CLUTCH...",
                            color = if (f1LitCount == 5) CoralWarning else TextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        Text("LIGHTS OUT!", color = TextInverse, fontSize = 42.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("RELEASE CLUTCH!", color = TextInverse, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Text("JUMP START FALSE START", color = CoralWarning, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.CNS_TAP -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                if (!isCnsRunning && cnsTimeRemainingSec == 10) {
                    Text("10s CNS Readiness Test", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap the circular pad as rapidly as possible for 10 seconds. Quantifies central nervous system fatigue and motor velocity.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onStartCns() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp).width(180.dp)
                    ) {
                        Text("START TEST", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Text("$cnsTimeRemainingSec s", color = SignalAmber, fontSize = 42.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Taps: $cnsTapCount", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
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
                                                onCnsTap()
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
                                            onValidResponse(true, hwTime)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(20.dp).background(DarkBackground, CircleShape))
                    }
                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Track parafoveal target...", color = TextMuted, fontSize = 14.sp)
                    }
                } else if (drillState == DrillState.TRIAL_FEEDBACK) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 42.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onAdvanceTrial() },
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
        DrillType.FLASH_GRID -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (drillState == DrillState.TRIAL_FEEDBACK) {
                    Text(outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 38.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onAdvanceTrial() },
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
                                                        onEarlyTap()
                                                    } else if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                        change?.consume()
                                                        onValidResponse(isFlashing, hwTime)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFlashing) {
                                    Box(modifier = Modifier.size(16.dp).background(DarkBackground, CircleShape))
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun CognitiveStimulusView(
    drillType: DrillType,
    drillState: DrillState,
    lastFeedbackMs: Long,
    lastFeedbackMsg: String,
    isGoStimulus: Boolean,
    choiceTargetDirection: Int,
    stroopWord: String,
    stroopColorIndex: Int,
    stroopColorValues: List<Color>,
    evenOddNumber: Int,
    flankerCenterRight: Boolean,
    flankerCongruent: Boolean,
    onStartNextTrial: () -> Unit,
    onAdvanceTrial: () -> Unit
) {
    when (drillType) {
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
                            onClick = { onStartNextTrial() },
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
                            color = if (listOf("Missed", "Error", "Anticipation", "Too Soon").any { lastFeedbackMsg.contains(it, ignoreCase = true) }) CoralWarning else SportGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
                    Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 42.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onAdvanceTrial() },
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
        DrillType.STROOP -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(CharcoalCard, CircleShape)
                                .border(2.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = TextMuted, fontSize = 36.sp, fontWeight = FontWeight.Light)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Focus on ink color · Ignore written word", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        Text(
                            text = stroopWord,
                            color = stroopColorValues.getOrElse(stroopColorIndex) { CoralWarning },
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("TAP INK COLOR BELOW", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.EVEN_ODD -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(CharcoalCard, CircleShape)
                                .border(2.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("?", color = TextMuted, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Numerical parity discrimination", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        Text(text = "$evenOddNumber", color = BrandAccent, fontSize = 72.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SELECT EVEN OR ODD", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.FLANKER -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(CharcoalCard, CircleShape)
                                .border(2.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = TextMuted, fontSize = 32.sp, fontWeight = FontWeight.Light)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Respond to CENTER arrow direction only", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        val centerSymbol = if (flankerCenterRight) "▶" else "◀"
                        val flankerSymbol = if (flankerCongruent) centerSymbol else (if (flankerCenterRight) "◀" else "▶")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(flankerSymbol, color = TextSubtle, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            Text(flankerSymbol, color = TextSubtle, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            Text(centerSymbol, color = BrandAccent, fontSize = 48.sp, fontWeight = FontWeight.Black)
                            Text(flankerSymbol, color = TextSubtle, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            Text(flankerSymbol, color = TextSubtle, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("MATCH CENTER ARROW", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.RHYTHM_SYNC -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS -> {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(CharcoalCard, CircleShape)
                                .border(2.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Anticipation Timing Synchronization", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Strike trigger at exactly 0 ms coincidence", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        Text("INTERCEPT TARGET", color = BrandAccent, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap trigger at the exact millisecond of arrival", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        else -> {}
    }
}

@Composable
private fun SpecializedBatteryStimulusView(
    drillType: DrillType,
    drillState: DrillState,
    lastFeedbackMs: Long,
    lastFeedbackMsg: String,
    choice4WayDirection: Int,
    matchColorNames: List<String>,
    matchColorValues: List<Color>,
    colorMatchIndex: Int,
    gridTrackingTargetIndex: Int,
    spatialAudioChannel: Int,
    activeQuadrantIndex: Int,
    onStartNextTrial: () -> Unit,
    onAdvanceTrial: () -> Unit,
    onValidResponse: (isCorrect: Boolean, hwTime: Long) -> Unit,
    onEarlyTap: () -> Unit
) {
    when (drillType) {
        DrillType.CHOICE_4WAY -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS -> {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(CharcoalCard, CircleShape)
                                .border(2.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("4-Way Choice Stimulus", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Prepare cardinal directional vectors (↑, ↓, ←, →)", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        val dirs = listOf("UP", "RIGHT", "DOWN", "LEFT")
                        val dirIcons = listOf(Icons.Default.ArrowUpward, Icons.AutoMirrored.Filled.ArrowForward, Icons.Default.ArrowDownward, Icons.AutoMirrored.Filled.ArrowBack)
                        val targetDir = dirs.getOrElse(choice4WayDirection) { "UP" }
                        val targetIcon = dirIcons.getOrElse(choice4WayDirection) { Icons.Default.ArrowUpward }

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(BrandAccent.copy(alpha = 0.15f), CircleShape)
                                .border(3.dp, BrandAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = targetIcon, contentDescription = targetDir, tint = BrandAccent, modifier = Modifier.size(64.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("STRIKE $targetDir", color = BrandAccent, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.COLOR_MATCH -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS -> {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(CharcoalCard, CircleShape)
                                .border(2.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Chromatic Hue Discrimination", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Match the displayed flash color with response tile", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        val activeColor = matchColorValues.getOrElse(colorMatchIndex) { CoralWarning }
                        val activeName = matchColorNames.getOrElse(colorMatchIndex) { "RED" }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(110.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(activeColor)
                                .border(3.dp, Color.White, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = activeName, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("SELECT $activeName TILE", color = activeColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.GRID_TRACKING -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS, DrillState.STIMULUS_ACTIVE -> {
                        Text(
                            text = if (drillState == DrillState.STIMULUS_ACTIVE) "TAP HIGHLIGHTED TARGET" else "FIXATE GRID MATRIX",
                            color = if (drillState == DrillState.STIMULUS_ACTIVE) BrandAccent else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (row in 0 until 4) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (col in 0 until 4) {
                                        val idx = row * 4 + col
                                        val isTarget = (drillState == DrillState.STIMULUS_ACTIVE && idx == gridTrackingTargetIndex)
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isTarget) BrandAccent else CharcoalCardElevated)
                                                .border(
                                                    if (isTarget) 2.dp else 1.dp,
                                                    if (isTarget) Color.White else BorderSubtle,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .pointerInput(drillState, gridTrackingTargetIndex) {
                                                    awaitPointerEventScope {
                                                        while (true) {
                                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                                            if (event.type == PointerEventType.Press) {
                                                                val change = event.changes.firstOrNull()
                                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                                    change?.consume()
                                                                    onValidResponse(idx == gridTrackingTargetIndex, hwTime)
                                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                                    change?.consume()
                                                                    onEarlyTap()
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isTarget) {
                                                Box(modifier = Modifier.size(24.dp).background(Color.White, CircleShape))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.SPATIAL_AUDIO -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS -> {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(CharcoalCard, CircleShape)
                                .border(2.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Binaural Acoustic Spatial Vector", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Listen for tone in LEFT or RIGHT audio channel", color = TextMuted, fontSize = 13.sp)
                    }
                    DrillState.STIMULUS_ACTIVE -> {
                        val isLeft = (spatialAudioChannel == 0)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isLeft) BrandAccent else CharcoalCardElevated)
                                    .border(if (isLeft) 2.dp else 1.dp, if (isLeft) Color.White else BorderSubtle, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = if (isLeft) DarkBackground else TextMuted, modifier = Modifier.size(28.dp))
                                    Text("LEFT EAR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isLeft) DarkBackground else TextMuted)
                                }
                            }

                            Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(48.dp))

                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (!isLeft) BrandAccent else CharcoalCardElevated)
                                    .border(if (!isLeft) 2.dp else 1.dp, if (!isLeft) Color.White else BorderSubtle, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = if (!isLeft) DarkBackground else TextMuted, modifier = Modifier.size(28.dp))
                                    Text("RIGHT EAR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (!isLeft) DarkBackground else TextMuted)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = if (isLeft) "TONE ACTIVE IN LEFT EAR" else "TONE ACTIVE IN RIGHT EAR",
                            color = BrandAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        DrillType.QUADRANT_CHOICE -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (drillState) {
                    DrillState.WAITING_FOR_STIMULUS, DrillState.STIMULUS_ACTIVE -> {
                        Text(
                            text = if (drillState == DrillState.STIMULUS_ACTIVE) "TAP ACTIVE QUADRANT NOW" else "FIXATE CENTER CROSSHAIR",
                            color = if (drillState == DrillState.STIMULUS_ACTIVE) BrandAccent else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (row in 0..1) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    for (col in 0..1) {
                                        val qIndex = row * 2 + col
                                        val isTarget = (drillState == DrillState.STIMULUS_ACTIVE && qIndex == activeQuadrantIndex)
                                        Box(
                                            modifier = Modifier
                                                .size(130.dp, 100.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (isTarget) BrandAccent else CharcoalCardElevated)
                                                .border(
                                                    if (isTarget) 3.dp else 1.dp,
                                                    if (isTarget) Color.White else BorderSubtle,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .pointerInput(drillState, activeQuadrantIndex) {
                                                    awaitPointerEventScope {
                                                        while (true) {
                                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                                            if (event.type == PointerEventType.Press) {
                                                                val change = event.changes.firstOrNull()
                                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                                    change?.consume()
                                                                    onValidResponse(qIndex == activeQuadrantIndex, hwTime)
                                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                                    change?.consume()
                                                                    onEarlyTap()
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = listOf("Q1 (TL)", "Q2 (TR)", "Q3 (BL)", "Q4 (BR)")[qIndex],
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isTarget) DarkBackground else TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    DrillState.TOO_SOON, DrillState.FALSE_START -> {
                        Button(
                            onClick = { onStartNextTrial() },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAlert, contentColor = DarkBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Trial", fontWeight = FontWeight.Bold)
                        }
                    }
                    DrillState.TRIAL_FEEDBACK -> {
                        Text(text = outcomeGlyph(lastFeedbackMsg) + "  " + latencyLabel(lastFeedbackMs), color = feedbackColor(lastFeedbackMsg, BrandAccent), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAdvanceTrial() },
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
        else -> {}
    }
}

/**
 * A trial with no recorded latency was never answered, so it must not be printed as
 * "0 ms" - that reads like an impossibly fast result rather than a miss.
 */
internal fun latencyLabel(ms: Long): String = if (ms <= 0L) "Missed" else "$ms ms"

/**
 * Success and failure must survive colour blindness.
 *
 * Roughly 8% of men cannot reliably separate the green and red used for hit and miss, so
 * the outcome is also carried by a glyph that reads without any colour at all.
 */
internal fun outcomeGlyph(msg: String): String = if (isFailureFeedback(msg)) "✕" else "✓"

/** Whether a feedback line describes a failed trial rather than a successful one. */
internal fun isFailureFeedback(msg: String): Boolean =
    listOf("Missed", "Error", "Incorrect", "Wrong", "Anticipation", "Too Soon", "Commission", "Failed")
        .any { msg.contains(it, ignoreCase = true) }

/** Success colour for a good trial, warning colour for a failed one. */
internal fun feedbackColor(msg: String, success: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color =
    if (isFailureFeedback(msg)) CoralWarning else success
