package com.example.ui.components

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillInfo
import com.example.model.DrillMode
import com.example.model.TrainingRules
import com.example.model.supportsTrainMode
import com.example.model.DrillType
import com.example.model.NotificationItem
import com.example.model.PerformanceMetric
import com.example.model.totalTrialsCount
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CoolBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SportGreen
import com.example.ui.theme.TextInverse
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachInsightSheet(
    insightText: String,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalCard,
        tonalElevation = 8.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BrandAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Psychology,
                            contentDescription = null,
                            tint = BrandAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Neuromuscular Analysis",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalCardElevated, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Psychology, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "OBSERVATION",
                            color = AmberAlert,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your choice speed is trailing your visual speed.",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = BrandAccent, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Synthesizing neural model...", color = TextMuted, fontSize = 14.sp)
                }
            } else {
                Text(
                    text = insightText,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrillDetailSheet(
    drill: DrillInfo,
    soundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onStartDrill: (DrillMode) -> Unit,
    onHowScoringWorks: () -> Unit,
    onDismiss: () -> Unit
) {
    // Test is the default: a new drill should first be measured, then trained.
    var selectedMode by remember(drill.type) { mutableStateOf(DrillMode.TEST) }
    val canTrain = drill.type.supportsTrainMode
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalCard,
        tonalElevation = 8.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(CharcoalCardElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = BrandAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = drill.type.title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = drill.subtitle, color = TextMuted, fontSize = 13.sp)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Purpose
            Text(
                text = drill.purpose,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mode picker. Test measures, Train trains - see DrillMode for why one
            // format cannot honestly do both.
            if (canTrain) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "MODE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val trials = drill.type.totalTrialsCount
                    listOf(
                        Triple(DrillMode.TEST, "Test", "$trials trials · measured"),
                        Triple(DrillMode.TRAIN, "Train", "${TrainingRules.TOTAL_TIME_SEC.toInt()}s · survive")
                    ).forEach { (modeOption, label, caption) ->
                        val isSelected = selectedMode == modeOption
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandAccent else CharcoalCardElevated)
                                .border(1.dp, if (isSelected) BrandAccent else BorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { selectedMode = modeOption }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) TextInverse else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = caption,
                                color = if (isSelected) TextInverse.copy(alpha = 0.75f) else TextSubtle,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (selectedMode == DrillMode.TEST) {
                        "Fixed protocol, no difficulty ramp - this is the run that sets your benchmark and trend."
                    } else {
                        "The clock is the only way out. Correct responses buy time, mistakes cost it, and it speeds up as you level. Not counted toward your benchmark."
                    },
                    color = TextSubtle,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "PROTOCOL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CharcoalCardElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val trials = drill.type.totalTrialsCount
                    listOf(
                        "$trials trial${if (trials == 1) "" else "s"}",
                        drill.defaultDuration,
                        "Target ${drill.targetBenchmark}"
                    ).forEach { detail ->
                        Text(text = detail, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metrics preview
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "METRICS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalCardElevated, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Median", "Accuracy", "Consistency").forEach { metric ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SportGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = metric, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sound cues toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalCardElevated, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Sound cues", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = onSoundToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextInverse,
                        checkedTrackColor = BrandAccent,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderSubtle
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Start drill button
            Button(
                onClick = { onStartDrill(selectedMode) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_drill_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Start Drill", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary How scoring works
            TextButton(
                onClick = onHowScoringWorks,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Scoring guide", color = CoolBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringWorksSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalCard,
        tonalElevation = 8.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "How Scoring Works", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val items = listOf(
                Pair("Median Latency", "We use median instead of single fastest tap to eliminate accidental or outlier clicks. True skill is repeatable speed."),
                Pair("Accuracy Factor", "Incorrect choices or target misses add a progressive penalty to ensure you balance raw speed with motor precision."),
                Pair("Consistency Variance", "Consistency measures the standard deviation across trials. A lower variance demonstrates athletic discipline."),
                Pair("RPI (Reaction Performance Index)", "A unified 0–1000 rating combining your speed, choice flexibility, accuracy, and streak bonuses.")
            )

            items.forEach { (title, desc) ->
                Column(modifier = Modifier.padding(bottom = 14.dp)) {
                    Text(text = title, color = BrandAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = desc, color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Got it", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<NotificationItem>,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalCard,
        tonalElevation = 8.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Notifications", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onMarkAllRead) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "All read", color = BrandAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (notifications.isEmpty()) {
                Text(
                    text = "No notifications yet. You'll see updates here as you train.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(notifications) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CharcoalCardElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, if (item.isUnread) BrandAccent.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(8.dp)
                                    .background(if (item.isUnread) BrandAccent else Color.Transparent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = item.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = item.timeAgo, color = TextSubtle, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = item.description, color = TextMuted, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricDetailSheet(
    metric: PerformanceMetric,
    onDismiss: () -> Unit
) {
    var selectedRange by remember { mutableStateOf("30D") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalCard,
        tonalElevation = 8.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${metric.name} Analysis", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time range selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7D", "30D", "90D", "All").forEach { range ->
                    val isSelected = selectedRange == range
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandAccent else CharcoalCardElevated)
                            .clickable { selectedRange = range }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range,
                            color = if (isSelected) TextInverse else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalCardElevated, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "SCORE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "${metric.score} / 100", color = BrandAccent, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = metric.detailValue, color = CoolBlue, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "ABOUT", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = metric.description, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "TREND", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = metric.trendDescription, color = TextPrimary, fontSize = 13.sp, lineHeight = 20.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationSheet(
    touchSamplingOffsetMs: Long = 0L,
    panelLatencyMs: Long = 0L,
    onSaveCalibration: (touchSamplingOffsetMs: Long, panelLatencyMs: Long, refreshHz: Int) -> Unit = { _, _, _ -> },
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val refreshRateHz = remember { view.display?.refreshRate?.roundToInt()?.takeIf { it > 0 } ?: 60 }
    val frameDurationMs = remember(refreshRateHz) { 1000f / refreshRateHz }

    // Intervals between consecutive touch samples during a drag. This is the digitiser's
    // reporting period - a real, measurable property of the hardware.
    val sampleIntervals = remember { mutableStateListOf<Long>() }
    var manualPanelLatency by remember { mutableStateOf(panelLatencyMs) }

    val touchPeriodMs = remember(sampleIntervals.size) {
        if (sampleIntervals.size >= 8) {
            // Median: a finger pausing mid-drag produces outliers a mean would follow.
            sampleIntervals.sorted()[sampleIntervals.size / 2].toFloat()
        } else null
    }
    // A sample is stamped up to one period late, half a period on average.
    val touchOffsetMs = touchPeriodMs?.let { (it / 2f).roundToInt().toLong() }
    val hasEnoughSamples = touchOffsetMs != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalCard,
        tonalElevation = 8.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(text = "Timing calibration", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reaction times run from the vsync of the frame carrying the stimulus to the kernel timestamp of your touch. This removes the two overheads left inside that window.",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            CalibrationRow(
                title = "Display refresh",
                value = "$refreshRateHz Hz",
                detail = "One frame is " + "%.1f".format(frameDurationMs) + " ms. Stimulus onset is timed from the real vsync, so this no longer adds error."
            )

            Spacer(modifier = Modifier.height(10.dp))

            CalibrationRow(
                title = "Touch sampling",
                value = if (touchPeriodMs != null) "%.1f".format(touchPeriodMs) + " ms" else "not measured",
                detail = if (touchOffsetMs != null) {
                    "Digitiser reports every " + "%.1f".format(touchPeriodMs) + " ms, so a contact is stamped about " + touchOffsetMs + " ms late."
                } else {
                    "Drag slowly inside the box below to sample your digitiser's reporting rate."
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCardElevated)
                    .border(1.dp, if (hasEnoughSamples) SportGreen else BorderSubtle, RoundedCornerShape(14.dp))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            var previous = 0L
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull()
                                if (change == null) continue
                                if (!change.pressed) {
                                    previous = 0L
                                    continue
                                }
                                val t = change.uptimeMillis
                                if (previous != 0L) {
                                    val delta = t - previous
                                    // Ignore the gap across a lifted finger.
                                    if (delta in 1..60) sampleIntervals.add(delta)
                                }
                                previous = t
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hasEnoughSamples) "Sampled ${sampleIntervals.size} touch events" else "Drag here (${sampleIntervals.size}/8)",
                    color = if (hasEnoughSamples) SportGreen else TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CalibrationRow(
                title = "Panel latency",
                value = if (manualPanelLatency > 0L) "$manualPanelLatency ms" else "not set",
                detail = "The delay between vsync and pixels emitting light cannot be measured on this device - that needs a high-speed camera. It stays out of your results unless you enter a figure you measured yourself."
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0L, 5L, 10L, 15L).forEach { option ->
                    val selected = manualPanelLatency == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) BrandAccent else CharcoalCardElevated)
                            .border(1.dp, if (selected) BrandAccent else BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { manualPanelLatency = option }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (option == 0L) "None" else "$option ms",
                            color = if (selected) TextInverse else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val total = (touchOffsetMs ?: 0L) + manualPanelLatency
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalCardElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(text = "TOTAL CORRECTION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$total ms", color = BrandAccent, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Subtracted from every future measurement. Results already recorded are left as they were.",
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onSaveCalibration(touchOffsetMs ?: 0L, manualPanelLatency, refreshRateHz)
                    onDismiss()
                },
                enabled = hasEnoughSamples || manualPanelLatency > 0L,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_calibration_button")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply calibration", fontWeight = FontWeight.Bold)
            }

            if (touchSamplingOffsetMs > 0L || panelLatencyMs > 0L) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Currently applied: ${touchSamplingOffsetMs + panelLatencyMs} ms",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CalibrationRow(title: String, value: String, detail: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalCardElevated)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = value, color = BrandAccent, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = detail, color = TextSubtle, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
