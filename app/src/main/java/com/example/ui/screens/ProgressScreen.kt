package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import com.example.ui.theme.CoralWarning
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import com.example.data.SessionEntity
import com.example.model.DrillType
import com.example.model.PerformanceMetric
import com.example.model.UserProfile
import com.example.model.drillTypeFromId
import com.example.model.isChoiceCategory
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    userProfile: UserProfile,
    sessions: List<SessionEntity>,
    selectedRange: String,
    onRangeSelect: (String) -> Unit,
    onMetricClick: (PerformanceMetric) -> Unit,
    onBuildSession: () -> Unit,
    onViewAllRecords: () -> Unit,
    onExportCsv: () -> Unit = {},
    onDeleteSession: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    // A run can be discarded, but never silently: deleting changes the trend.
    var pendingDelete by remember { mutableStateOf<SessionEntity?>(null) }

    val metrics = remember(sessions) { buildPerformanceMetrics(sessions) }
    val trendPoints = remember(sessions) { sessions.asReversed().takeLast(7).map { it.medianTimeMs.toFloat() } }
    val weakestMetric = remember(metrics) { metrics.minBy { it.score } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Page title + segmented time range control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            // Segmented control: 7D, 30D, 90D, All
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf("7D", "30D", "90D", "All").forEach { range ->
                    val isSelected = selectedRange == range
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BrandAccent else Color.Transparent)
                            .clickable { onRangeSelect(range) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("progress_range_${range.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range,
                            color = if (isSelected) TextInverse else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Main trend card: line chart of median reaction time
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("main_trend_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "MEDIAN",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = trendPoints.lastOrNull()?.let { "${it.roundToInt()} ms" } ?: "--",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (trendPoints.size >= 2) {
                        val deltaMs = (trendPoints.first() - trendPoints.last()).roundToInt()
                        val improving = deltaMs >= 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background((if (improving) SportGreen else AmberAlert).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (improving) SportGreen else AmberAlert,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (improving) "-$deltaMs ms" else "+${-deltaMs} ms",
                                color = if (improving) SportGreen else AmberAlert,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (trendPoints.size >= 2) {
                    // Chart Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val points = trendPoints
                        val min = points.min()
                        val max = points.max()
                        val range = (max - min).coerceAtLeast(1f)

                        // Horizontal reference grid lines
                        val stepY = size.height / 3
                        for (i in 0..3) {
                            val y = i * stepY
                            drawLine(
                                color = BorderSubtle,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Trend path
                        val path = Path()
                        val widthStep = size.width / (points.size - 1)

                        points.forEachIndexed { i, p ->
                            val x = i * widthStep
                            val y = ((p - min) / range) * size.height
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        // Gradient under path
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(BrandAccent.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )

                        drawPath(
                            path = path,
                            color = BrandAccent,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Dots
                        points.forEachIndexed { i, p ->
                            val x = i * widthStep
                            val y = ((p - min) / range) * size.height
                            drawCircle(
                                color = CharcoalCard,
                                radius = 5.dp.toPx(),
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = if (i == points.size - 1) BrandAccent else CoolBlue,
                                radius = 3.5.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Complete a few sessions to see your reaction-time trend.",
                        color = TextSubtle,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 3. Performance profile (four horizontal metric rows)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "PERFORMANCE PROFILE",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            metrics.forEach { metric ->
                val metricIcon = when (metric.name) {
                    "Speed" -> Icons.Default.Bolt
                    "Accuracy" -> Icons.Default.CheckCircle
                    "Consistency" -> Icons.Default.Timeline
                    else -> Icons.Default.AltRoute
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .clickable { onMetricClick(metric) }
                        .padding(16.dp)
                        .testTag("metric_row_${metric.name.lowercase()}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(CharcoalCardElevated, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = metricIcon, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = metric.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = metric.detailValue, color = TextMuted, fontSize = 11.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${metric.score}%", color = if (metric.score >= 80) SportGreen else AmberAlert, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { metric.score / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (metric.score >= 80) SportGreen else AmberAlert,
                            trackColor = BorderSubtle
                        )
                    }
                }
            }
        }

        // 4. Coach analysis card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp)
                .testTag("coach_analysis_card")
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COACH PRESCRIPTION",
                        color = AmberAlert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (sessions.isEmpty()) "Complete your first session" else "Focus: ${weakestMetric.name}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (sessions.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "+${100 - weakestMetric.score}% target", color = TextMuted, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AltRoute, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = recommendedDrillLabel(weakestMetric.name), color = BrandAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onBuildSession,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("build_session_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Build session", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. Records row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${userProfile.fastestMs}ms", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${userProfile.totalSessions}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${userProfile.streakDays}d", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(
                    onClick = onViewAllRecords,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View all", tint = CoolBlue, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Coach Research CSV Export Action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CharcoalCardElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                .clickable { onExportCsv() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("coach_csv_export_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BrandAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Export Research Data (CSV)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Raw millisecond timestamps, CV%, and IAAF rule violations", color = TextSubtle, fontSize = 11.sp)
                    }
                }
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(16.dp))
            }
        }

        // 6. History
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RECENT SESSIONS",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CharcoalCard, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Your first result starts here.", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                sessions.take(6).forEach { session ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(CharcoalCardElevated, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = session.drillTitle, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (session.athleteName.isNotBlank() && session.athleteName != "Self") {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "· ${session.athleteName}", color = CoolBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = dateFormat.format(Date(session.timestamp)), color = TextSubtle, fontSize = 11.sp)
                                        if (session.cvPercent > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "· CV ${session.cvPercent}%", color = if (session.cvPercent < 8f) SportGreen else TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        if (session.falseStarts > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "· ${session.falseStarts} FS", color = AmberAlert, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "${session.medianTimeMs} ms", color = SportGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${session.accuracyPercent}% acc", color = TextMuted, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { pendingDelete = session },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Discard this session",
                                        tint = TextSubtle,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { session ->
        DeleteSessionDialog(
            session = session,
            onConfirm = {
                onDeleteSession(session.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun DeleteSessionDialog(
    session: SessionEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Discard", color = CoralWarning, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Keep", color = TextMuted)
            }
        },
        title = { Text(text = "Discard this run?", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "${session.drillTitle} · ${session.medianTimeMs} ms will be removed from your history, and your trend and averages will be recalculated without it. This cannot be undone.",
                color = TextMuted,
                fontSize = 13.sp
            )
        },
        containerColor = CharcoalCard,
        shape = RoundedCornerShape(14.dp)
    )
}

private fun recommendedDrillLabel(metricName: String): String = when (metricName) {
    "Decision" -> "Choice drills"
    "Speed" -> "Visual Reflex drills"
    "Consistency" -> "Rhythm & pacing drills"
    "Accuracy" -> "Go/No-Go drills"
    else -> "Reaction drills"
}

private fun scoreAgainstBenchmark(avgMedianMs: Double, benchmarkMs: Int): Int =
    ((benchmarkMs / avgMedianMs) * 100).roundToInt().coerceIn(0, 100)

internal fun buildPerformanceMetrics(sessions: List<SessionEntity>): List<PerformanceMetric> {
    val classicSessions = sessions.filter { it.drillId == DrillType.CLASSIC.id }
    val choiceSessions = sessions.filter { s -> drillTypeFromId(s.drillId)?.isChoiceCategory == true }

    val speedMetric = if (classicSessions.isNotEmpty()) {
        val avgMedian = classicSessions.map { it.medianTimeMs }.average()
        val bestMs = classicSessions.minOf { s -> if (s.bestTimeMs > 0) s.bestTimeMs else s.medianTimeMs }
        val trend = if (classicSessions.size >= 4) {
            val half = classicSessions.size / 2
            val recentAvg = classicSessions.take(half).map { it.medianTimeMs }.average()
            val olderAvg = classicSessions.takeLast(classicSessions.size - half).map { it.medianTimeMs }.average()
            val diffPct = ((olderAvg - recentAvg) / olderAvg) * 100
            when {
                diffPct > 0.5 -> "+${"%.1f".format(Locale.US, diffPct)}% faster over your recent sessions."
                diffPct < -0.5 -> "${"%.1f".format(Locale.US, -diffPct)}% slower over your recent sessions."
                else -> "Holding steady over your recent sessions."
            }
        } else "Complete more Visual Reflex sessions to see a trend."
        PerformanceMetric(
            name = "Speed",
            score = scoreAgainstBenchmark(avgMedian, 200),
            detailValue = "Median ${avgMedian.roundToInt()} ms · Best $bestMs ms",
            trendDescription = trend,
            description = "Raw neuromuscular transmission latency to visual triggers. Measured in milliseconds."
        )
    } else {
        PerformanceMetric(
            name = "Speed",
            score = 0,
            detailValue = "No data yet",
            trendDescription = "Complete a Visual Reflex drill to start tracking.",
            description = "Raw neuromuscular transmission latency to visual triggers. Measured in milliseconds."
        )
    }

    val accuracyMetric = if (sessions.isNotEmpty()) {
        val avgAcc = sessions.map { it.accuracyPercent }.average()
        PerformanceMetric(
            name = "Accuracy",
            score = avgAcc.roundToInt().coerceIn(0, 100),
            detailValue = "${avgAcc.roundToInt()}% correct response rate",
            trendDescription = "Averaged across ${sessions.size} recorded session${if (sessions.size == 1) "" else "s"}.",
            description = "The ratio of correct inputs without anticipating triggers prematurely or selecting the wrong path."
        )
    } else {
        PerformanceMetric(
            name = "Accuracy",
            score = 0,
            detailValue = "No data yet",
            trendDescription = "Complete a drill to start tracking.",
            description = "The ratio of correct inputs without anticipating triggers prematurely or selecting the wrong path."
        )
    }

    val consistencyMetric = if (sessions.isNotEmpty()) {
        val avgConsistency = sessions.map { it.consistencyMs }.average()
        PerformanceMetric(
            name = "Consistency",
            score = (100 - avgConsistency).roundToInt().coerceIn(0, 100),
            detailValue = "${avgConsistency.roundToInt()} ms standard deviation",
            trendDescription = "Averaged across ${sessions.size} recorded session${if (sessions.size == 1) "" else "s"}.",
            description = "Standard variance between trials. Elite competitors demonstrate sub-15ms consistency."
        )
    } else {
        PerformanceMetric(
            name = "Consistency",
            score = 0,
            detailValue = "No data yet",
            trendDescription = "Complete a drill to start tracking.",
            description = "Standard variance between trials. Elite competitors demonstrate sub-15ms consistency."
        )
    }

    val decisionMetric = if (choiceSessions.isNotEmpty()) {
        val avgMedian = choiceSessions.map { it.medianTimeMs }.average()
        val trailing = if (classicSessions.isNotEmpty()) {
            val visualAvg = classicSessions.map { it.medianTimeMs }.average()
            val gap = (avgMedian - visualAvg).roundToInt()
            if (gap > 0) "Trailing visual speed by $gap ms." else "Matching or beating visual speed."
        } else "Complete a Visual Reflex drill to compare against choice speed."
        PerformanceMetric(
            name = "Decision",
            score = scoreAgainstBenchmark(avgMedian, 260),
            detailValue = "${avgMedian.roundToInt()} ms choice latency",
            trendDescription = trailing,
            description = "Cognitive discrimination speed when resolving multi-choice stimulus prompts."
        )
    } else {
        PerformanceMetric(
            name = "Decision",
            score = 0,
            detailValue = "No data yet",
            trendDescription = "Complete a Choice Reaction drill to start tracking.",
            description = "Cognitive discrimination speed when resolving multi-choice stimulus prompts."
        )
    }

    return listOf(speedMetric, accuracyMetric, consistencyMetric, decisionMetric)
}
