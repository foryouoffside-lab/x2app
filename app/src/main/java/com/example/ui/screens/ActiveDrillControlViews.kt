package com.example.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillState
import com.example.model.DrillType
import com.example.ui.theme.*

@Composable
fun ActiveDrillControlViews(
    drillType: DrillType,
    drillState: DrillState,
    choiceTargetDirection: Int,
    stroopColors: List<String>,
    stroopColorValues: List<Color>,
    stroopColorIndex: Int,
    evenOddNumber: Int,
    flankerCenterRight: Boolean,
    choice4WayDirection: Int,
    matchColorNames: List<String>,
    matchColorValues: List<Color>,
    colorMatchIndex: Int,
    spatialAudioChannel: Int,
    onValidResponse: (isCorrect: Boolean, hwTime: Long) -> Unit,
    onEarlyTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                                                onValidResponse(choiceTargetDirection == 0, hwTime)
                                            } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                change?.consume()
                                                onEarlyTap()
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
                                                onValidResponse(choiceTargetDirection == 1, hwTime)
                                            } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                change?.consume()
                                                onEarlyTap()
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

            DrillType.STROOP -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stroopColors.forEachIndexed { idx, name ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(stroopColorValues[idx])
                                .pointerInput(drillState, stroopColorIndex) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type == PointerEventType.Press) {
                                                val change = event.changes.firstOrNull()
                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                    change?.consume()
                                                    onValidResponse(idx == stroopColorIndex, hwTime)
                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                    change?.consume()
                                                    onEarlyTap()
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("stroop_btn_${name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = if (idx == 1 || idx == 3) DarkBackground else Color.White
                            )
                        }
                    }
                }
            }

            DrillType.EVEN_ODD -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    listOf(true to "EVEN", false to "ODD").forEach { (isEvenTarget, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalCardElevated)
                                .border(1.5.dp, BrandAccent, RoundedCornerShape(14.dp))
                            .pointerInput(drillState, evenOddNumber) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        if (event.type == PointerEventType.Press) {
                                            val change = event.changes.firstOrNull()
                                            val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                            if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                change?.consume()
                                                val correct = ((evenOddNumber % 2 == 0) == isEvenTarget)
                                                onValidResponse(correct, hwTime)
                                            } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                change?.consume()
                                                onEarlyTap()
                                            }
                                        }
                                    }
                                }
                            }
                            .testTag("even_odd_${label.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = label, fontWeight = FontWeight.Black, fontSize = 17.sp, color = TextPrimary)
                        }
                    }
                }
            }

            DrillType.FLANKER -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    listOf(false to "◀ LEFT", true to "RIGHT ▶").forEach { (isRight, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalCardElevated)
                                .border(1.5.dp, BrandAccent, RoundedCornerShape(14.dp))
                                .pointerInput(drillState, flankerCenterRight) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type == PointerEventType.Press) {
                                                val change = event.changes.firstOrNull()
                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                    change?.consume()
                                                    val correct = (flankerCenterRight == isRight)
                                                    onValidResponse(correct, hwTime)
                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                    change?.consume()
                                                    onEarlyTap()
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("flanker_${if (isRight) "right" else "left"}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = label, fontWeight = FontWeight.Black, fontSize = 17.sp, color = TextPrimary)
                        }
                    }
                }
            }

            DrillType.CLASSIC, DrillType.AUDITORY, DrillType.GO_NO_GO,
            DrillType.TACTILE, DrillType.F1_LIGHTS, DrillType.RHYTHM_SYNC -> {
                val padLabel = when (drillType) {
                    DrillType.F1_LIGHTS -> if (drillState == DrillState.STIMULUS_ACTIVE) "RELEASE CLUTCH NOW!" else "HOLD CLUTCH DOWN"
                    DrillType.TACTILE -> if (drillState == DrillState.STIMULUS_ACTIVE) "VIBRATION ACTIVE · TAP!" else "HOLD FOR VIBRATION"
                    DrillType.RHYTHM_SYNC -> if (drillState == DrillState.STIMULUS_ACTIVE) "SYNC STRIKE (0 ms)!" else "AWAIT BASELINE"
                    else -> if (drillState == DrillState.STIMULUS_ACTIVE) "TAP TRIGGER!" else "TACTILE RESPONSE ZONE"
                }
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
                                            onValidResponse(true, hwTime)
                                        } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                            change?.consume()
                                            onEarlyTap()
                                        }
                                    }
                                }
                            }
                        }
                        .testTag("primary_trigger_pad"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = padLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (drillState == DrillState.STIMULUS_ACTIVE) TextInverse else TextPrimary
                    )
                }
            }

            DrillType.CHOICE_4WAY -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // UP (direction 0)
                    Box(
                        modifier = Modifier
                            .size(80.dp, 52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalCardElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .pointerInput(drillState, choice4WayDirection) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        if (event.type == PointerEventType.Press) {
                                            val change = event.changes.firstOrNull()
                                            val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                            if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                change?.consume()
                                                onValidResponse(choice4WayDirection == 0, hwTime)
                                            } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                change?.consume()
                                                onEarlyTap()
                                            }
                                        }
                                    }
                                }
                            }
                            .testTag("key_up"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "UP", tint = TextPrimary, modifier = Modifier.size(24.dp))
                    }

                    // Row: LEFT (3), DOWN (2), RIGHT (1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp, 52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalCardElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .pointerInput(drillState, choice4WayDirection) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type == PointerEventType.Press) {
                                                val change = event.changes.firstOrNull()
                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                    change?.consume()
                                                    onValidResponse(choice4WayDirection == 3, hwTime)
                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                    change?.consume()
                                                    onEarlyTap()
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("key_left"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "LEFT", tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(80.dp, 52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalCardElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .pointerInput(drillState, choice4WayDirection) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type == PointerEventType.Press) {
                                                val change = event.changes.firstOrNull()
                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                    change?.consume()
                                                    onValidResponse(choice4WayDirection == 2, hwTime)
                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                    change?.consume()
                                                    onEarlyTap()
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("key_down"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "DOWN", tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(80.dp, 52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalCardElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .pointerInput(drillState, choice4WayDirection) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type == PointerEventType.Press) {
                                                val change = event.changes.firstOrNull()
                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                    change?.consume()
                                                    onValidResponse(choice4WayDirection == 1, hwTime)
                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                    change?.consume()
                                                    onEarlyTap()
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("key_right"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "RIGHT", tint = TextPrimary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            DrillType.COLOR_MATCH -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    matchColorNames.forEachIndexed { index, colorName ->
                        val colorVal = matchColorValues[index]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colorVal.copy(alpha = 0.25f))
                                .border(2.dp, colorVal, RoundedCornerShape(14.dp))
                                .pointerInput(drillState, colorMatchIndex) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type == PointerEventType.Press) {
                                                val change = event.changes.firstOrNull()
                                                val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                                if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                    change?.consume()
                                                    onValidResponse(index == colorMatchIndex, hwTime)
                                                } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                    change?.consume()
                                                    onEarlyTap()
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("color_match_$colorName"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = colorName, fontWeight = FontWeight.Black, fontSize = 13.sp, color = colorVal)
                        }
                    }
                }
            }

            DrillType.SPATIAL_AUDIO -> {
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
                            .pointerInput(drillState, spatialAudioChannel) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        if (event.type == PointerEventType.Press) {
                                            val change = event.changes.firstOrNull()
                                            val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                            if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                change?.consume()
                                                onValidResponse(spatialAudioChannel == 0, hwTime)
                                            } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                change?.consume()
                                                onEarlyTap()
                                            }
                                        }
                                    }
                                }
                            }
                            .testTag("spatial_left"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LEFT EAR", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CharcoalCardElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .pointerInput(drillState, spatialAudioChannel) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        if (event.type == PointerEventType.Press) {
                                            val change = event.changes.firstOrNull()
                                            val hwTime = change?.uptimeMillis ?: SystemClock.uptimeMillis()
                                            if (drillState == DrillState.STIMULUS_ACTIVE) {
                                                change?.consume()
                                                onValidResponse(spatialAudioChannel == 1, hwTime)
                                            } else if (drillState == DrillState.WAITING_FOR_STIMULUS) {
                                                change?.consume()
                                                onEarlyTap()
                                            }
                                        }
                                    }
                                }
                            }
                            .testTag("spatial_right"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("RIGHT EAR", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            DrillType.GRID_TRACKING, DrillType.QUADRANT_CHOICE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCardElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ON-CANVAS DIRECT TOUCH ACTIVE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextMuted,
                        letterSpacing = 1.sp
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
