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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.model.DrillType
import com.example.model.UserProfile
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import com.example.ui.components.DrillPreviewAnimation
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SportGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.VisionTeal

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    sessions: List<SessionEntity> = emptyList(),
    onStartSession: (DrillType) -> Unit,
    onPlayChallenge: () -> Unit,
    coachHeadline: String = "",
    recommendedDuration: String = "2 min",
    onOpenWhyCoach: () -> Unit,
    onNavigateToProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val dailyResetLabel = remember { timeUntilMidnightLabel() }
    val weeklyStats = remember(sessions) { computeWeeklyStats(sessions) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Greeting row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(CharcoalCard, CircleShape)
                        .border(1.dp, BorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BrandAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = userProfile.name.split(" ").firstOrNull().takeUnless { it.isNullOrBlank() } ?: "Athlete",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Streak chip (Flame icon in warm amber + streak value)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(CharcoalCardElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = AmberAlert,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${userProfile.streakDays}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // 2. Performance hero card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("performance_hero_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = BrandAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RPI",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    if (userProfile.totalSessions > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(BrandAccent.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${userProfile.totalSessions} session${if (userProfile.totalSessions == 1) "" else "s"}",
                                color = BrandAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${userProfile.rpi}",
                        color = TextPrimary,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black
                    )
                    // Only claim a direction when there is a real week-over-week comparison.
                    // "-x ms" means faster (an improvement); "+x ms" means slower.
                    if (userProfile.weeklyDelta.isNotBlank()) {
                        val improved = userProfile.weeklyDelta.startsWith("-")
                        val regressed = userProfile.weeklyDelta.startsWith("+")
                        val trendColor = when {
                            improved -> SportGreen
                            regressed -> AmberAlert
                            else -> TextMuted
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            if (improved || regressed) {
                                Icon(
                                    imageVector = if (improved) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = trendColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = userProfile.weeklyDelta,
                                color = trendColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (weeklyStats.reactionTrend.size >= 2) 16.dp else 6.dp))

                // Recent reaction-time trend (derived from actual session history; lower = faster)
                if (weeklyStats.reactionTrend.size >= 2) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        val points = weeklyStats.reactionTrend
                        val min = points.min()
                        val max = points.max()
                        val range = (max - min).coerceAtLeast(1f)

                        val path = Path()
                        val widthStep = size.width / (points.size - 1)

                        // Lower reaction time is better, so faster runs plot toward the top.
                        points.forEachIndexed { i, p ->
                            val x = i * widthStep
                            val y = (p - min) / range * size.height
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        // Draw subtle gradient fill under sparkline
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(BrandAccent.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )

                        // Draw sparkline stroke
                        drawPath(
                            path = path,
                            color = BrandAccent,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw end dot
                        val lastX = size.width
                        val lastY = (points.last() - min) / range * size.height
                        drawCircle(color = BrandAccent, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
                    }
                } else {
                    Text(
                        text = "Complete a few sessions to see your trend.",
                        color = TextSubtle,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 3. Coach card (subtle warm accent rule)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .testTag("coach_insight_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Warm left rule
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(78.dp)
                        .background(BrandAccent)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Psychology,
                                contentDescription = "Coach Insight",
                                tint = BrandAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COACH",
                                color = BrandAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = coachHeadline.ifBlank { "Complete a Visual Reflex and a Choice Reaction drill to unlock coach insights." },
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onOpenWhyCoach,
                        modifier = Modifier
                            .size(36.dp)
                            .background(CharcoalCardElevated, CircleShape)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .testTag("why_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Why?",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Recommended session card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .clickable { onStartSession(DrillType.CHOICE) }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("recommended_session_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Live Animated Preview
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCardElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    DrillPreviewAnimation(
                        drillType = DrillType.CHOICE,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Middle: Title & Timer
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Directional Choice",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = recommendedDuration,
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "·",
                            color = TextSubtle,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Recommended",
                            color = VisionTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right: Circular Play Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CharcoalCardElevated)
                        .border(1.dp, BorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Choice Reaction",
                        tint = VisionTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 5. Today's challenge card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .clickable { onPlayChallenge() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("daily_challenge_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Animated Thumbnail Preview
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCardElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    DrillPreviewAnimation(
                        drillType = DrillType.FLASH_GRID,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Middle: Title & Timer
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Flash Grid Challenge",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = dailyResetLabel,
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "·",
                            color = TextSubtle,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Resets daily",
                            color = TextSubtle,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right: Circular Play Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CharcoalCardElevated)
                        .border(1.dp, BorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Challenge",
                        tint = VisionTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 6. Progress strip (Tappable opens Progress)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CharcoalCardElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                .clickable { onNavigateToProgress() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("progress_strip")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (weeklyStats.medianDeltaLabel.isNotBlank()) {
                        // A "+" delta means this week's median is faster than last week's.
                        val improved = weeklyStats.medianDeltaLabel.startsWith("+")
                        val deltaColor = if (improved) SportGreen else AmberAlert
                        Icon(
                            imageVector = if (improved) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = "Trend",
                            tint = deltaColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = weeklyStats.medianDeltaLabel, color = deltaColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        Text(text = "Train this week to see your trend", color = TextMuted, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    // Mini bars: real sessions completed per day, last 7 days.
                    // Days with no sessions stay unfilled so the strip never implies activity.
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                        weeklyStats.dailyBars.forEach { (height, hasSessions) ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(height.dp)
                                    .background(
                                        if (hasSessions) BrandAccent else BorderSubtle,
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View progress",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private data class WeeklyStats(
    val reactionTrend: List<Float>,
    val medianDeltaLabel: String,
    val dailyBars: List<Pair<Int, Boolean>>
)

private fun computeWeeklyStats(sessions: List<SessionEntity>): WeeklyStats {
    val trend = sessions.asReversed().takeLast(7).map { it.medianTimeMs.toFloat() }

    val dayMillis = 24L * 60L * 60L * 1000L
    val today = System.currentTimeMillis() / dayMillis
    val dailyCounts = (6 downTo 0).map { offset ->
        val day = today - offset
        sessions.count { it.timestamp / dayMillis == day }
    }
    val maxCount = dailyCounts.maxOrNull()?.takeIf { it > 0 } ?: 1
    val bars = dailyCounts.map { count ->
        val height = if (count == 0) 4 else 6 + ((count.toFloat() / maxCount) * 14f).roundToInt()
        height to (count > 0)
    }

    val weekMillis = 7L * dayMillis
    val now = System.currentTimeMillis()
    val thisWeek = sessions.filter { now - it.timestamp <= weekMillis }
    val lastWeek = sessions.filter { now - it.timestamp > weekMillis && now - it.timestamp <= weekMillis * 2 }
    val deltaLabel = if (thisWeek.isNotEmpty() && lastWeek.isNotEmpty()) {
        val avgThis = thisWeek.map { it.medianTimeMs }.average()
        val avgLast = lastWeek.map { it.medianTimeMs }.average()
        val pct = ((avgLast - avgThis) / avgLast) * 100.0
        val sign = if (pct >= 0) "+" else ""
        "$sign${"%.1f".format(Locale.US, pct)}%"
    } else ""

    return WeeklyStats(trend, deltaLabel, bars)
}

internal fun timeUntilMidnightLabel(): String {
    val now = Calendar.getInstance()
    val midnight = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffMs = (midnight.timeInMillis - now.timeInMillis).coerceAtLeast(0)
    val hours = diffMs / 3_600_000
    val minutes = (diffMs % 3_600_000) / 60_000
    val seconds = (diffMs % 60_000) / 1000
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
