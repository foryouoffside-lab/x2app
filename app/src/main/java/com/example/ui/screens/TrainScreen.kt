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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillInfo
import com.example.model.DrillType
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

@Composable
fun TrainScreen(
    drills: List<DrillInfo>,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    onDrillClick: (DrillInfo) -> Unit,
    onStartRecommended: () -> Unit,
    onContinueTraining: () -> Unit,
    onYourPlanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val filterScrollState = rememberScrollState()

    val filteredDrills = drills.filter { drill ->
        when (selectedFilter) {
            "All" -> true
            "Visual" -> drill.type == DrillType.CLASSIC
            "Decision" -> drill.type == DrillType.CHOICE
            "Precision" -> drill.type == DrillType.PRECISION
            "Audio" -> false
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
            OutlinedButton(
                onClick = onYourPlanClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("your_plan_button")
            ) {
                Text(text = "Your plan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 2. Recommendation banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp)
                .testTag("train_recommendation_banner")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECOMMENDED DRILL",
                        color = CoolBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(ElectricLime.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "High Impact", color = ElectricLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Today’s focus: decision speed",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Choice reaction training to compress the 72ms motor selection latency delta.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartRecommended,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("start_8min_session_button")
                ) {
                    Text(text = "Start 8 min session", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Filter chips (Horizontally scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Visual", "Decision", "Precision", "Audio").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) ElectricLime else CharcoalCard)
                        .border(1.dp, if (isSelected) ElectricLime else BorderSubtle, RoundedCornerShape(50.dp))
                        .clickable { onFilterSelect(filter) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("filter_chip_${filter.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) DarkBackground else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // 4. Core drills section (2-column grid of 4 cards)
        Text(
            text = "CORE DRILLS",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // 2x2 grid layout
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val rows = filteredDrills.chunked(2)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { drill ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CharcoalCard)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                .clickable { onDrillClick(drill) }
                                .padding(16.dp)
                                .testTag("drill_card_${drill.type.id}")
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(CharcoalCardElevated, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = ElectricLime,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(BorderSubtle)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = drill.badge, color = ElectricLime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = drill.type.title,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = drill.subtitle,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CharcoalCardElevated, RoundedCornerShape(8.dp))
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Play", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 5. Continue training card (2 of 3 sessions this week)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("continue_training_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp)) {
                        CircularProgressIndicator(
                            progress = { 2f / 3f },
                            color = ElectricLime,
                            trackColor = BorderSubtle,
                            strokeWidth = 3.5.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(text = "2/3", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = "Weekly Schedule", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "2 of 3 sessions this week", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = onContinueTraining,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("continue_button")
                ) {
                    Text(text = "Continue", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Coming next row (muted cards for Audio Reaction and Moving Target)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "COMING NEXT",
                color = TextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("Audio Reaction" to "Sound latency test", "Moving Target" to "Dynamic tracking").forEach { (title, desc) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalCard.copy(alpha = 0.6f))
                            .border(1.dp, BorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = title, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(13.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = desc, color = TextSubtle, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(BorderSubtle, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "In development", color = TextSubtle, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
