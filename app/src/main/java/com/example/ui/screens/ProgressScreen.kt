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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import com.example.data.SessionEntity
import com.example.model.PerformanceMetric
import com.example.model.UserProfile
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CoolBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    userProfile: UserProfile,
    sessions: List<SessionEntity>,
    selectedRange: String,
    onRangeSelect: (String) -> Unit,
    onMetricClick: (PerformanceMetric) -> Unit,
    onBuildSession: () -> Unit,
    onViewAllRecords: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val metrics = listOf(
        PerformanceMetric(
            name = "Speed",
            score = 82,
            detailValue = "Median 236 ms · Best 214 ms",
            trendDescription = "+3.4% faster over the last 30 days.",
            description = "Raw neuromuscular transmission latency to visual triggers. Measured in milliseconds."
        ),
        PerformanceMetric(
            name = "Accuracy",
            score = 91,
            detailValue = "91% correct response rate",
            trendDescription = "Zero false triggers recorded on 4 out of last 5 runs.",
            description = "The ratio of correct inputs without anticipating triggers prematurely or selecting the wrong path."
        ),
        PerformanceMetric(
            name = "Consistency",
            score = 74,
            detailValue = "18 ms standard deviation",
            trendDescription = "Variability reduced from 28 ms to 18 ms.",
            description = "Standard variance between trials. Elite competitors demonstrate sub-15ms consistency."
        ),
        PerformanceMetric(
            name = "Decision",
            score = 66,
            detailValue = "286 ms choice latency",
            trendDescription = "Trailing visual speed by 72 ms. High priority focus area.",
            description = "Cognitive discrimination speed when resolving multi-choice stimulus prompts."
        )
    )

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
                            .background(if (isSelected) ElectricLime else Color.Transparent)
                            .clickable { onRangeSelect(range) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("progress_range_${range.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range,
                            color = if (isSelected) DarkBackground else TextMuted,
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
                        Text(
                            text = "MEDIAN REACTION TIME",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "236 ms",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(ElectricLime.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "12 ms faster than baseline", color = ElectricLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chart Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val points = listOf(254f, 250f, 246f, 248f, 242f, 238f, 236f)
                    val min = 230f
                    val max = 260f
                    val range = max - min

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
                            colors = listOf(CoolBlue.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    drawPath(
                        path = path,
                        color = CoolBlue,
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
                            color = if (i == points.size - 1) ElectricLime else CoolBlue,
                            radius = 3.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Today").forEach { day ->
                        Text(text = day, color = TextSubtle, fontSize = 10.sp)
                    }
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
                                Text(text = metric.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = metric.detailValue, color = TextMuted, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${metric.score} / 100", color = if (metric.score >= 80) ElectricLime else CoolBlue, fontSize = 14.sp, fontWeight = FontWeight.Black)
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
                            color = if (metric.score >= 80) ElectricLime else CoolBlue,
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
                Text(
                    text = "COACH PRESCRIPTION",
                    color = ElectricLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Best gain: visual reaction. Next focus: decision speed.",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Targeted drills to train motor cortex inhibition will close the trailing choice latency.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBuildSession,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("build_session_button")
                ) {
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "RECORDS & STATS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${userProfile.fastestMs} ms fastest · ${userProfile.totalSessions} sessions · ${userProfile.streakDays} day streak",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onViewAllRecords) {
                    Text(text = "View all", color = CoolBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 6. History
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "RECENT SESSIONS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

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
                                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = session.drillTitle, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = dateFormat.format(Date(session.timestamp)), color = TextSubtle, fontSize = 11.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "${session.medianTimeMs} ms", color = ElectricLime, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${session.accuracyPercent}% acc", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
