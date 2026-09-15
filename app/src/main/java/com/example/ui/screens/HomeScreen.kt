package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.AppTab
import com.example.model.AthleteProfile
import com.example.model.DrillType
import com.example.model.SportsBattery
import com.example.model.UserProfile
import com.example.ui.components.DrillPreviewAnimation
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
import com.example.ui.theme.VisionTeal

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    activeAthlete: AthleteProfile? = null,
    onOpenRoster: () -> Unit = {},
    batteries: List<SportsBattery> = emptyList(),
    onOpenBattery: (SportsBattery) -> Unit = {},
    onStartSession: (DrillType) -> Unit,
    onPlayChallenge: () -> Unit,
    onOpenWhyCoach: () -> Unit,
    onNavigateToProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Coach Roster Banner (Active Athlete Attribution)
        activeAthlete?.let { athlete ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CharcoalCardElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .clickable { onOpenRoster() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("coach_roster_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(BrandAccent.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = athlete.name.take(1), color = BrandAccent, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = athlete.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = athlete.countryFlag, fontSize = 12.sp)
                            }
                            Text(text = "${athlete.sport} · ${athlete.role} · Baseline ${athlete.baselineMs} ms", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(CharcoalCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Roster", color = CoolBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

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
                    text = userProfile.name.split(" ").firstOrNull() ?: "Alex",
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(BrandAccent.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = userProfile.rankLabel,
                            color = BrandAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = SportGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = userProfile.weeklyDelta,
                            color = SportGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7-day sparkline Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    val points = listOf(718f, 722f, 726f, 725f, 734f, 738f, 742f)
                    val min = 710f
                    val max = 750f
                    val range = max - min

                    val path = Path()
                    val widthStep = size.width / (points.size - 1)

                    points.forEachIndexed { i, p ->
                        val x = i * widthStep
                        val y = size.height - ((p - min) / range * size.height)
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
                    val lastY = size.height - ((points.last() - min) / range * size.height)
                    drawCircle(color = BrandAccent, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
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
                            text = "Choice speed trailing visual speed (-72 ms)",
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
                            text = "02:00",
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

        // Sports Combines & Protocols
        if (batteries.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                batteries.forEach { battery ->
                    val primaryDrill = battery.drills.firstOrNull() ?: DrillType.CLASSIC
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CharcoalCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .clickable { onOpenBattery(battery) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                    drillType = primaryDrill,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Middle: Title & Timer
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = battery.title,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = battery.durationMin,
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
                                        text = battery.sportTag,
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
                                    contentDescription = "Start ${battery.title}",
                                    tint = VisionTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
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
                            text = "08:42:16",
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
                            text = "Global",
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Trend",
                        tint = SportGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "+5.8%", color = SportGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                    // Mini sparkline bars
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                        listOf(8, 12, 10, 15, 14, 18, 20).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h.dp)
                                    .background(BrandAccent, RoundedCornerShape(1.dp))
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
