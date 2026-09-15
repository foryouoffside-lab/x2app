package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillInfo
import com.example.model.DrillType
import com.example.model.SportsBattery
import com.example.ui.components.DrillPreviewAnimation
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
fun TrainScreen(
    drills: List<DrillInfo>,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    onDrillClick: (DrillInfo) -> Unit,
    onStartRecommended: () -> Unit,
    onContinueTraining: () -> Unit,
    onYourPlanClick: () -> Unit,
    batteries: List<SportsBattery> = emptyList(),
    onOpenBattery: (SportsBattery) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val filterScrollState = rememberScrollState()

    val filteredDrills = drills.filter { drill ->
        when (selectedFilter) {
            "All" -> true
            "Visual" -> drill.type == DrillType.CLASSIC || drill.type == DrillType.FLASH_GRID
            "Inhibition" -> drill.type == DrillType.GO_NO_GO
            "Decision" -> drill.type == DrillType.CHOICE
            "Precision" -> drill.type == DrillType.PRECISION
            "Audio" -> drill.type == DrillType.AUDITORY
            "Motor CNS" -> drill.type == DrillType.CNS_TAP
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Page title + Your plan button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Train",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
            IconButton(
                onClick = onYourPlanClick,
                modifier = Modifier
                    .size(38.dp)
                    .background(CharcoalCard, CircleShape)
                    .border(1.dp, BorderSubtle, CircleShape)
                    .testTag("your_plan_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Your plan",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 2. Filter chips (Horizontally scrollable with icons)
        val filterItems = listOf(
            "All" to Icons.Default.GridView,
            "Visual" to Icons.Default.Visibility,
            "Inhibition" to Icons.Default.Block,
            "Audio" to Icons.AutoMirrored.Filled.VolumeUp,
            "Motor CNS" to Icons.Default.TouchApp,
            "Decision" to Icons.Default.AltRoute,
            "Precision" to Icons.Default.GpsFixed
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterItems.forEach { (filter, icon) ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) BrandAccent.copy(alpha = 0.22f) else CharcoalCardElevated)
                        .border(1.5.dp, if (isSelected) BrandAccent else BorderSubtle, RoundedCornerShape(50.dp))
                        .clickable { onFilterSelect(filter) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("filter_chip_${filter.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) BrandAccent else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = filter,
                            color = if (isSelected) TextPrimary else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. Core drills section (Single column vertical list - one drill above and one below)
        Text(
            text = "CORE DRILLS",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Single column vertical layout matching user reference
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            filteredDrills.forEach { drill ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                        .clickable { onDrillClick(drill) }
                        .padding(14.dp)
                        .testTag("drill_card_${drill.type.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Live Animated Drill Preview
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalCardElevated)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            DrillPreviewAnimation(
                                drillType = drill.type,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Middle: Drill Name + Timer underneath
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = drill.type.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Timer below the drill name
                            val timerText = when (drill.type) {
                                DrillType.CLASSIC -> "01:00"
                                DrillType.CHOICE -> "02:00"
                                DrillType.GO_NO_GO -> "02:00"
                                DrillType.AUDITORY -> "01:00"
                                DrillType.CNS_TAP -> "00:10"
                                DrillType.PRECISION -> "02:00"
                                DrillType.FLASH_GRID -> "02:00"
                            }
                            val trialsOrReps = if (drill.type == DrillType.CNS_TAP) "10s sprint" else "5 trials"

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = timerText,
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
                                    text = trialsOrReps,
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
                                contentDescription = "Start ${drill.type.title}",
                                tint = VisionTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Continue training card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .clickable { onContinueTraining() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("continue_training_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(38.dp)) {
                        CircularProgressIndicator(
                            progress = { 2f / 3f },
                            color = VisionTeal,
                            trackColor = BorderSubtle,
                            strokeWidth = 3.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(text = "2/3", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "Weekly Goal", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "02:00 left", color = TextMuted, fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CharcoalCardElevated)
                        .border(1.dp, BorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Continue", tint = VisionTeal, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 5. Coach Combine Protocols
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
    }
}
