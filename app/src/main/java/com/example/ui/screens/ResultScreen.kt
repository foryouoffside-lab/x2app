package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillMode
import com.example.model.DrillRunResult
import com.example.model.DrillType
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.CoolBlue
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SportGreen
import com.example.ui.theme.TextInverse
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
                "⚡ Athletic Reaction Protocol: ${result.medianTimeMs} ms median in ${result.drillType.title} (${result.athleteName})! CV%: ${result.cvPercent}%, Accuracy: ${result.accuracyPercent}%. #ReactionCoach"
            )
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Athlete Performance Data")
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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Top bar: Close (X) & Athlete Attribution
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

            // Athlete Attribution & Verification Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(CharcoalCardElevated)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(50.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = result.athleteName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 2. Main Metric Hero (Latency or CNS Frequency)
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
                if (result.drillType == DrillType.CNS_TAP && result.cnsFrequencyHz != null) {
                    Text(
                        text = "${result.cnsFrequencyHz} Hz",
                        color = BrandAccent,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CNS Frequency",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (result.mode == DrillMode.TRAIN) {
                    // A Train run ramps difficulty as it goes, so its median is not a
                    // measurement and must not headline the card. How long you lasted
                    // and how far you climbed are the real result.
                    Text(
                        text = "LVL ${result.levelReached}",
                        color = BrandAccent,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${result.survivedSec}s survived · ${result.rawTrials.size} trials",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = "${result.medianTimeMs} ms",
                        color = BrandAccent,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Median Reaction",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (result.mode == DrillMode.TRAIN) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Training run - not counted toward your benchmark.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (result.isPersonalBest) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(AmberAlert)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "PERSONAL BEST", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(SportGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = SportGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "±${result.consistencyMs} ms", color = SportGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                }
            }
        }

        // 3. Metrics Grid (CV%, IES, Attentional Tau, False Starts)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "METRICS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: CV% (Coefficient of Variation)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "MOTOR CV", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${result.cvPercent}%", color = if (result.cvPercent < 8f) SportGreen else TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Metric 2: Inverse Efficiency Score (IES)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "IES SPEED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${result.inverseEfficiencyScore} ms", color = CoolBlue, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 3: Attentional Tau
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "TAU LAPSE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${result.exGaussianTau} ms", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Metric 4: False Starts
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "FALSE STARTS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${result.falseStarts}",
                            color = if (result.falseStarts == 0) SportGreen else CoralWarning,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 4. Raw Trials Breakdown
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "TRIALS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(
                        text = if (result.rawTrials.isNotEmpty()) "${result.rawTrials.size}" else "--",
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (result.rawTrials.size > 10) {
                    // A long run would bury the card under dozens of rows. Show the shape
                    // of the run instead: every trial as a bar, plus the counts that matter.
                    TrialShapeSummary(result)
                } else if (result.rawTrials.isNotEmpty()) {
                    result.rawTrials.forEach { trial ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Trial ${trial.trialIndex}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (trial.isFalseStart) {
                                    Text(
                                        text = "ANTICIPATION",
                                        color = AmberAlert,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (!trial.isCorrect) {
                                    Text(
                                        text = "ERROR",
                                        color = CoralWarning,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = if (trial.latencyMs > 0) "${trial.latencyMs} ms" else "Withheld",
                                color = if (trial.isCorrect) BrandAccent else CoralWarning,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Per-trial data wasn't recorded for this run; don't fabricate individual trials.
                    Text(
                        text = "Per-trial breakdown wasn't recorded for this run. Median: ${result.medianTimeMs} ms · Best: ${result.bestTimeMs} ms.",
                        color = TextSubtle,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // 5. Coach Observation
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
                        .size(36.dp)
                        .background(BrandAccent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "COACH INSIGHT", color = BrandAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
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

        // 6. Action buttons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onTrainAgain,
                colors = ButtonDefaults.buttonColors(containerColor = BrandAccent, contentColor = TextInverse),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("train_again_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Train Again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { shareCard() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("share_result_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Share Card", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            TextButton(
                onClick = onViewInProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_in_progress_button")
            ) {
                Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = CoolBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "View Analytics", color = CoolBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


/**
 * Compact view of a long run.
 *
 * Each valid trial is one bar, so the eye reads the run's shape - steady, drifting, or
 * falling apart - without scrolling past forty rows. "Fade" compares the first third of
 * the run with the last third: on a Train run the difficulty is climbing throughout, so
 * slowing down late is expected and only a large gap is worth acting on.
 */
@Composable
private fun TrialShapeSummary(result: DrillRunResult) {
    val trials = result.rawTrials
    val valid = trials.filter { it.isCorrect && !it.isFalseStart && it.latencyMs > 0 }
    val errors = trials.count { !it.isCorrect && !it.isFalseStart }
    val anticipations = trials.count { it.isFalseStart }

    val slowest = (valid.maxOfOrNull { it.latencyMs } ?: 1L).coerceAtLeast(1L)
    val fastest = valid.minOfOrNull { it.latencyMs } ?: 0L

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            trials.forEach { trial ->
                val isMiss = !trial.isCorrect || trial.isFalseStart
                // Faster is better, so a quick trial draws a taller bar.
                val ratio = if (isMiss || trial.latencyMs <= 0) 1f
                else (trial.latencyMs.toFloat() / slowest.toFloat()).coerceIn(0.12f, 1f)
                val heightFraction = if (isMiss) 1f else (1.12f - ratio).coerceIn(0.15f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(heightFraction)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            when {
                                trial.isFalseStart -> AmberAlert
                                !trial.isCorrect -> CoralWarning
                                else -> BrandAccent
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TrialStat("HITS", "${valid.size}", BrandAccent)
            TrialStat("MISSED", "$errors", if (errors > 0) CoralWarning else TextMuted)
            TrialStat("EARLY", "$anticipations", if (anticipations > 0) AmberAlert else TextMuted)
            TrialStat("FASTEST", if (fastest > 0) "$fastest ms" else "--", TextPrimary)
        }

        if (valid.size >= 6) {
            val third = valid.size / 3
            val early = valid.take(third).map { it.latencyMs }.average()
            val late = valid.takeLast(third).map { it.latencyMs }.average()
            val fade = (late - early).toInt()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when {
                    fade > 40 -> "Fade: $fade ms slower by the end of the run."
                    fade < -40 -> "Warm-up: ${-fade} ms faster by the end of the run."
                    else -> "Held pace across the run (${if (fade >= 0) "+" else ""}$fade ms)."
                },
                color = TextSubtle,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TrialStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}
