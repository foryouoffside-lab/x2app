package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrillType
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalCardElevated
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SpeedCyan
import com.example.ui.theme.TextInverse
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSubtle
import kotlinx.coroutines.delay
import java.util.Locale

data class TutorialPhase(
    val title: String,
    val instruction: String,
    val coachSpeech: String
)

@Composable
fun DrillTutorialOverlay(
    drillType: DrillType,
    onStartExercise: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val phases = remember(drillType) {
        when (drillType) {
            DrillType.CLASSIC -> listOf(
                TutorialPhase(
                    title = "Positioning",
                    instruction = "Hover your thumb 1 cm above the strike sensor.",
                    coachSpeech = "Get set. Position your thumb directly above the strike pad."
                ),
                TutorialPhase(
                    title = "Perception",
                    instruction = "Gaze at the center reticle. Do not anticipate early.",
                    coachSpeech = "Maintain focus on the center reticle. Wait for the signal."
                ),
                TutorialPhase(
                    title = "Motor Strike",
                    instruction = "The microsecond cyan flashes, strike with peak velocity.",
                    coachSpeech = "The instant the screen flashes cyan, strike immediately with maximum velocity."
                )
            )
            DrillType.CHOICE -> listOf(
                TutorialPhase(
                    title = "Dual Readiness",
                    instruction = "Position left and right thumbs on the bottom quadrants.",
                    coachSpeech = "Prepare both hands. Position thumbs over the left and right quadrants."
                ),
                TutorialPhase(
                    title = "Directional Processing",
                    instruction = "Process the arrow vector the instant it flashes.",
                    coachSpeech = "An arrow will flash pointing left or right. Rapidly process its direction."
                ),
                TutorialPhase(
                    title = "Target Strike",
                    instruction = "Tap the matching quadrant. Accuracy and speed both matter.",
                    coachSpeech = "Strike the corresponding side instantly. Do not sacrifice accuracy for speed."
                )
            )
            DrillType.GO_NO_GO -> listOf(
                TutorialPhase(
                    title = "Cognitive Readiness",
                    instruction = "Maintain high alertness for target vs distractor cues.",
                    coachSpeech = "Inhibition control protocol. Prepare for high-speed impulse control."
                ),
                TutorialPhase(
                    title = "Cyan = GO",
                    instruction = "If the circle flashes Cyan, strike immediately!",
                    coachSpeech = "When the reticle flashes cyan, strike as fast as possible."
                ),
                TutorialPhase(
                    title = "Amber = NO-GO",
                    instruction = "If the circle flashes Amber, suppress motor impulse and hold!",
                    coachSpeech = "When the reticle flashes amber, hold back. Inhibit your movement completely."
                )
            )
            DrillType.PRECISION -> listOf(
                TutorialPhase(
                    title = "Visual Scanning",
                    instruction = "Hold your gaze broad to catch coordinate appearance.",
                    coachSpeech = "Saccadic targeting drill. Keep your gaze broad across the screen."
                ),
                TutorialPhase(
                    title = "Coordinate Acquisition",
                    instruction = "Target reticle appears at random screen coordinates.",
                    coachSpeech = "A target will flash at an unpredictable location."
                ),
                TutorialPhase(
                    title = "Foveal Precision",
                    instruction = "Steer your eye and thumb directly into the bullseye center.",
                    coachSpeech = "Steer rapidly and strike the bullseye center with pinpoint precision."
                )
            )
            DrillType.FLASH_GRID -> listOf(
                TutorialPhase(
                    title = "Fixation Anchor",
                    instruction = "Lock your vision strictly onto the center crosshair.",
                    coachSpeech = "Useful field of view test. Lock your eyes on the center crosshair."
                ),
                TutorialPhase(
                    title = "Parafoveal Detection",
                    instruction = "Use side peripheral vision to detect the flashed grid cell.",
                    coachSpeech = "Do not move your eyes. Use your peripheral vision to detect the flashing tile."
                ),
                TutorialPhase(
                    title = "Peripheral Strike",
                    instruction = "Tap the illuminated cell before stimulus decays.",
                    coachSpeech = "Strike the illuminated coordinate before it decays."
                )
            )
            DrillType.AUDITORY -> listOf(
                TutorialPhase(
                    title = "Acoustic Priming",
                    instruction = "Look slightly away from the display to isolate hearing.",
                    coachSpeech = "Acoustic reflex protocol. Focus solely on sound."
                ),
                TutorialPhase(
                    title = "Tone Frequency",
                    instruction = "Listen intently for the sharp starter tone.",
                    coachSpeech = "Listen for the starter tone frequency."
                ),
                TutorialPhase(
                    title = "Instant Strike",
                    instruction = "Tap anywhere on the display the instant you hear the tone.",
                    coachSpeech = "Tap anywhere on the screen the microsecond you hear the beep."
                )
            )
            DrillType.CNS_TAP -> listOf(
                TutorialPhase(
                    title = "Hand Stabilization",
                    instruction = "Rest your wrist securely for rapid repetitive motor firing.",
                    coachSpeech = "Central nervous system test. Rest your wrist in a stable position."
                ),
                TutorialPhase(
                    title = "Cadence Execution",
                    instruction = "Tap the circular pad with maximum motor cadence.",
                    coachSpeech = "Tap the sensor pad as rapidly as humanly possible."
                ),
                TutorialPhase(
                    title = "10s Stamina",
                    instruction = "Maintain peak tapping frequency for the full 10 seconds.",
                    coachSpeech = "Maintain your highest firing rate for ten consecutive seconds."
                )
            )
            DrillType.F1_LIGHTS -> listOf(
                TutorialPhase(
                    title = "Gantry Countdown",
                    instruction = "Watch the 5 red lights illuminate sequentially across the gantry.",
                    coachSpeech = "Five red lights will illuminate sequentially across the gantry. Hold your reaction."
                ),
                TutorialPhase(
                    title = "Anticipation Control",
                    instruction = "Do not jump the start! All 5 lights will hold for a randomized interval.",
                    coachSpeech = "Hold steady. An unpredictable delay occurs once all five red lights are on."
                ),
                TutorialPhase(
                    title = "Lights Out Release",
                    instruction = "The microsecond the red lights shut off, strike immediately!",
                    coachSpeech = "Lights out and away we go! Strike the instant the gantry extinguishes."
                )
            )
            DrillType.STROOP -> listOf(
                TutorialPhase(
                    title = "Semantic Interference",
                    instruction = "You will see a color word printed in a conflicting font color.",
                    coachSpeech = "Stroop color conflict test. A word will flash in a conflicting font color."
                ),
                TutorialPhase(
                    title = "Inhibit Reading",
                    instruction = "Suppress the urge to read the text. Focus solely on ink color.",
                    coachSpeech = "Inhibit the urge to read the word. Identify the font ink color only."
                ),
                TutorialPhase(
                    title = "Chromatic Strike",
                    instruction = "Tap the bottom button matching the font color as fast as possible.",
                    coachSpeech = "Tap the matching color button immediately to beat the clock."
                )
            )
            DrillType.EVEN_ODD -> listOf(
                TutorialPhase(
                    title = "Number Flash",
                    instruction = "A random two-digit number will flash in the center of the display.",
                    coachSpeech = "A random number will flash. Process its numerical value instantly."
                ),
                TutorialPhase(
                    title = "Parity Classification",
                    instruction = "Determine if the number is Even (ends in 0,2,4,6,8) or Odd.",
                    coachSpeech = "Classify whether the number is Even or Odd without mental hesitation."
                ),
                TutorialPhase(
                    title = "Binary Decision",
                    instruction = "Tap the EVEN or ODD button with explosive decision velocity.",
                    coachSpeech = "Strike the correct parity button with explosive decision velocity."
                )
            )
            DrillType.FLANKER -> listOf(
                TutorialPhase(
                    title = "Target Isolation",
                    instruction = "A row of 5 arrows will flash. Focus your attention strictly on the CENTER arrow.",
                    coachSpeech = "Eriksen flanker test. Lock your attention strictly onto the center arrow."
                ),
                TutorialPhase(
                    title = "Filter Distractors",
                    instruction = "Flanking arrows will attempt to mislead your directional choice.",
                    coachSpeech = "Surrounding flanker arrows will try to mislead you. Inhibit their interference."
                ),
                TutorialPhase(
                    title = "Directional Execution",
                    instruction = "Strike LEFT or RIGHT based exclusively on the central arrow direction.",
                    coachSpeech = "Tap Left or Right according to the center arrow vector only."
                )
            )
            DrillType.RHYTHM_SYNC -> listOf(
                TutorialPhase(
                    title = "Kinematic Tracking",
                    instruction = "A velocity cursor descends towards the baseline at constant speed.",
                    coachSpeech = "Anticipation timing protocol. Track the descending cursor kinematic path."
                ),
                TutorialPhase(
                    title = "Temporal Calibration",
                    instruction = "Synchronize your internal clock with the descending movement.",
                    coachSpeech = "Synchronize your internal rhythm with the cursor's arrival velocity."
                ),
                TutorialPhase(
                    title = "Zero-Millisecond Hit",
                    instruction = "Tap at the exact millisecond the cursor meets the baseline (0 ms target).",
                    coachSpeech = "Strike at the exact millisecond of arrival. Strive for zero millisecond error."
                )
            )
        }
    }

    var currentPhaseIndex by remember { mutableIntStateOf(0) }
    var isVoiceEnabled by remember { mutableStateOf(true) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Initialize TextToSpeech
    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setSpeechRate(1.05f)
                    tts?.setPitch(1.0f)
                    isTtsReady = true
                }
            }
            ttsInstance = tts
        } catch (_: Exception) {}

        onDispose {
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (_: Exception) {}
        }
    }

    // Function to speak a phrase
    fun speakCurrentPhrase(text: String) {
        if (!isVoiceEnabled || !isTtsReady) return
        try {
            ttsInstance?.stop()
            ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tutorial_utterance")
        } catch (_: Exception) {}
    }

    // Automatic phase synchronization loop
    LaunchedEffect(currentPhaseIndex, isTtsReady, isVoiceEnabled) {
        if (currentPhaseIndex in phases.indices) {
            val phase = phases[currentPhaseIndex]
            speakCurrentPhrase(phase.coachSpeech)
            // Wait while current phase is active, then auto advance or loop
            delay(3600L)
            if (currentPhaseIndex < phases.size - 1) {
                currentPhaseIndex++
            }
        }
    }

    // Clean exit/start function that guarantees voiceover stops
    fun handleStartExercise() {
        try {
            ttsInstance?.stop()
        } catch (_: Exception) {}
        onStartExercise()
    }

    fun handleExit() {
        try {
            ttsInstance?.stop()
        } catch (_: Exception) {}
        onExit()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("drill_tutorial_overlay")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Exit, Coach Tag, Voice Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { handleExit() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Tutorial", tint = TextMuted)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(CharcoalCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = SpeedCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "COACH BRIEFING",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = {
                        isVoiceEnabled = !isVoiceEnabled
                        if (!isVoiceEnabled) {
                            try { ttsInstance?.stop() } catch (_: Exception) {}
                        } else {
                            speakCurrentPhrase(phases[currentPhaseIndex].coachSpeech)
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isVoiceEnabled) SpeedCyan.copy(alpha = 0.15f) else CharcoalCard)
                        .border(1.dp, if (isVoiceEnabled) SpeedCyan else BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isVoiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle Voice-Over",
                        tint = if (isVoiceEnabled) SpeedCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Drill Title & Category
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = drillType.title,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = drillType.category,
                        color = SpeedCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "·", color = TextSubtle, fontSize = 12.sp)
                    Text(
                        text = "Neuro-Perceptual Calibration",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Visual Demonstration Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CharcoalCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Animated Live Mechanics Demo
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(CharcoalCardElevated)
                            .border(1.5.dp, SpeedCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DrillPreviewAnimation(
                            drillType = drillType,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phase Indicator Tabs (Step 1, 2, 3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        phases.forEachIndexed { index, _ ->
                            val isSelected = index == currentPhaseIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isSelected) SpeedCyan else BorderSubtle)
                                    .clickable {
                                        currentPhaseIndex = index
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Synchronized Instruction Card
                    val activePhase = phases[currentPhaseIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isVoiceEnabled) {
                                LiveEqualizerWave(modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "PHASE ${currentPhaseIndex + 1}: ${activePhase.title.uppercase()}",
                                color = SignalAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = activePhase.instruction,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Actions: Start Drill & Replay
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { handleStartExercise() },
                    colors = ButtonDefaults.buttonColors(containerColor = SpeedCyan, contentColor = TextInverse),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_drill_exercise_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "START EXERCISE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            currentPhaseIndex = 0
                            speakCurrentPhrase(phases[0].coachSpeech)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Replay Briefing", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Voice auto-mutes in drill",
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact animated audio equalizer wave to indicate synchronized coach voice-over.
 */
@Composable
private fun LiveEqualizerWave(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "equalizer_wave")
    val bar1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar3"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barWidth = w / 5f
        val gap = w / 10f

        // 3 equalizer bars
        val heights = listOf(bar1, bar2, bar3)
        heights.forEachIndexed { index, scale ->
            val left = index * (barWidth + gap)
            val barH = h * scale
            val top = (h - barH) / 2f
            drawRect(
                color = SpeedCyan,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barH)
            )
        }
    }
}
