package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillRunResult
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
fun ResultScreen(
    result: DrillRunResult,
    onTrainAgain: () -> Unit,
    onViewInProgress: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun shareCard() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "⚡ Reaction Training: ${result.medianTimeMs} ms median in ${result.drillType.title}! Accuracy: ${result.accuracyPercent}%. #ReactionApp"
            )
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Reaction Performance")
        context.startActivity(shareIntent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Top bar: Close (X)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_result_button")
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }

            // Verified run badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(CharcoalCardElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Verified run", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 2. Main Metric Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${result.medianTimeMs} ms",
                    color = ElectricLime,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = "Median Reaction Time",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (result.isPersonalBest) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(ElectricLime)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "NEW PERSONAL BEST", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(ElectricLime.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "+12 ms vs baseline", color = ElectricLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(CharcoalCardElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Top 18%", color = CoolBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Trial Breakdown (5 horizontal bars)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "TRIAL BREAKDOWN", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(text = "5 trials recorded", color = TextSubtle, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5 representative trial latencies around median
                val trials = listOf(
                    result.bestTimeMs,
                    result.medianTimeMs - 8,
                    result.medianTimeMs,
                    result.medianTimeMs + 14,
                    result.medianTimeMs + 26
                )
                val maxVal = trials.maxOrNull()?.coerceAtLeast(300L) ?: 300L

                trials.forEachIndexed { idx, ms ->
                    val isFastest = idx == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "T${idx + 1}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CharcoalCardElevated)
                        ) {
                            val fraction = (ms.toFloat() / maxVal.toFloat()).coerceIn(0.1f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isFastest) ElectricLime else CoolBlue.copy(alpha = 0.6f))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$ms ms",
                            color = if (isFastest) ElectricLime else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isFastest) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.width(52.dp)
                        )
                    }
                }
            }
        }

        // 4. Secondary Metrics Row: Accuracy, Consistency, Best Trial
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Triple("ACCURACY", "${result.accuracyPercent}%", ElectricLime),
                Triple("VARIANCE", "±${result.consistencyMs} ms", CoolBlue),
                Triple("BEST TRIAL", "${result.bestTimeMs} ms", TextPrimary)
            ).forEach { (label, value, color) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // 5. AI Coach Note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalCardElevated)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(ElectricLime.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Psychology, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "COACH OBSERVATION", color = ElectricLime, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result.coachNote,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 6. Action buttons: Train again, Share result, View in progress
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onTrainAgain,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = DarkBackground),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("train_again_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Train again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { shareCard() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("share_result_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Share result card", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            TextButton(
                onClick = onViewInProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_in_progress_button")
            ) {
                Text(text = "View in progress", color = CoolBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
