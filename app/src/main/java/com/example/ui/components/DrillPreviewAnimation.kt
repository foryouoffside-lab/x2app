package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.DrillType
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.CoolBlue
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SportGreen
import com.example.ui.theme.VisionTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.CharcoalCardElevated
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance animated preview of drills matching the actual exercise behavior.
 * Rendered using Compose Canvas with lightweight infinite transitions.
 */
@Composable
fun DrillPreviewAnimation(
    drillType: DrillType,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "drill_preview_${drillType.id}")

    when (drillType) {
        DrillType.CLASSIC -> {
            // Visual Reflex / Eye Blink & Photon Pulse
            val blinkProgress by transition.animateFloat(
                initialValue = 0.05f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "classic_blink"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h * 0.5f
                val openHeight = (h * 0.28f) * blinkProgress

                // Eyelid line paths (open to gentle squeeze)
                val topPath = Path().apply {
                    moveTo(w * 0.15f, midY)
                    quadraticTo(w * 0.5f, midY - openHeight, w * 0.85f, midY)
                }
                val bottomPath = Path().apply {
                    moveTo(w * 0.15f, midY)
                    quadraticTo(w * 0.5f, midY + openHeight, w * 0.85f, midY)
                }

                // Draw outer contour in subtle teal
                drawPath(
                    path = topPath,
                    color = VisionTeal.copy(alpha = 0.85f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = bottomPath,
                    color = VisionTeal.copy(alpha = 0.85f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Pupil / Photon flash dot in center
                val pupilRadius = (w * 0.09f) * blinkProgress.coerceAtLeast(0.2f)
                drawCircle(
                    color = VisionTeal,
                    radius = pupilRadius,
                    center = Offset(w * 0.5f, midY)
                )

                // Concentric photon glow when fully open
                if (blinkProgress > 0.7f) {
                    val glowAlpha = (blinkProgress - 0.7f) / 0.3f * 0.4f
                    drawCircle(
                        color = VisionTeal.copy(alpha = glowAlpha),
                        radius = pupilRadius * 2.2f,
                        center = Offset(w * 0.5f, midY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }

        DrillType.AUDITORY -> {
            // Clock dial timer & acoustic ripple sweep
            val sweepAngle by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "auditory_sweep"
            )

            val pulseRing by transition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "auditory_pulse"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.5f)
                val baseRadius = w * 0.32f

                // Outer faint clock ring
                drawCircle(
                    color = VisionTeal.copy(alpha = 0.25f),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Expanding acoustic wave ring
                drawCircle(
                    color = VisionTeal.copy(alpha = (1.2f - pulseRing).coerceIn(0f, 0.5f)),
                    radius = baseRadius * pulseRing,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Sweeping clock arc
                drawArc(
                    color = VisionTeal,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                    size = Size(baseRadius * 2, baseRadius * 2),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Center pivot dot
                drawCircle(
                    color = VisionTeal,
                    radius = 2.5.dp.toPx(),
                    center = center
                )

                // Clock ticking hand
                val rad = Math.toRadians((sweepAngle - 90.0)).toFloat()
                val handLength = baseRadius * 0.7f
                val endX = center.x + handLength * cos(rad)
                val endY = center.y + handLength * sin(rad)
                drawLine(
                    color = VisionTeal,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        DrillType.PRECISION -> {
            // Saccadic Target Push-Up / Moving Reticle Target
            val targetProgress by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "precision_motion"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Guide track line
                drawLine(
                    color = VisionTeal.copy(alpha = 0.2f),
                    start = Offset(w * 0.18f, h * 0.5f),
                    end = Offset(w * 0.82f, h * 0.5f),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Moving focal target
                val currentX = w * targetProgress
                val currentY = h * 0.5f
                val focalOffset = Offset(currentX, currentY)

                // Outer pulsing crosshair ring
                drawCircle(
                    color = VisionTeal.copy(alpha = 0.35f),
                    radius = 9.dp.toPx(),
                    center = focalOffset,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Inner bright focal dot
                drawCircle(
                    color = VisionTeal,
                    radius = 4.5.dp.toPx(),
                    center = focalOffset
                )

                // Crosshair tick marks
                val tickLen = 4.dp.toPx()
                drawLine(
                    color = VisionTeal,
                    start = Offset(currentX - 9.dp.toPx() - tickLen, currentY),
                    end = Offset(currentX - 9.dp.toPx() + 1.dp.toPx(), currentY),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = VisionTeal,
                    start = Offset(currentX + 9.dp.toPx() - 1.dp.toPx(), currentY),
                    end = Offset(currentX + 9.dp.toPx() + tickLen, currentY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }

        DrillType.FLASH_GRID -> {
            // 3x3 Parafoveal Flash Grid / Brock String Sequence
            val flashStep by transition.animateFloat(
                initialValue = 0f,
                targetValue = 9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2700, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "grid_flash_step"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val activeIndex = flashStep.toInt() % 9

                val padX = w * 0.22f
                val padY = h * 0.22f
                val stepX = (w - padX * 2) / 2
                val stepY = (h - padY * 2) / 2

                for (row in 0..2) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val cx = padX + col * stepX
                        val cy = padY + row * stepY

                        if (index == activeIndex) {
                            // Active flashing cell with glowing aura
                            drawCircle(
                                color = VisionTeal.copy(alpha = 0.35f),
                                radius = 7.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = VisionTeal,
                                radius = 4.5.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        } else {
                            // Inactive subtle grid cell
                            drawCircle(
                                color = Color.White.copy(alpha = 0.2f),
                                radius = 2.5.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }
            }
        }

        DrillType.CHOICE -> {
            // Directional Decision Latency (CRT) - Alternating Left / Right vectors
            val directionSide by transition.animateFloat(
                initialValue = 0f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "choice_switch"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h * 0.5f
                val isLeftActive = directionSide < 1f

                // Left Arrow Chevron
                val leftColor = if (isLeftActive) VisionTeal else VisionTeal.copy(alpha = 0.25f)
                val leftPath = Path().apply {
                    moveTo(w * 0.36f, midY - 8.dp.toPx())
                    lineTo(w * 0.24f, midY)
                    lineTo(w * 0.36f, midY + 8.dp.toPx())
                }
                drawPath(
                    path = leftPath,
                    color = leftColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Right Arrow Chevron
                val rightColor = if (!isLeftActive) VisionTeal else VisionTeal.copy(alpha = 0.25f)
                val rightPath = Path().apply {
                    moveTo(w * 0.64f, midY - 8.dp.toPx())
                    lineTo(w * 0.76f, midY)
                    lineTo(w * 0.64f, midY + 8.dp.toPx())
                }
                drawPath(
                    path = rightPath,
                    color = rightColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Center decision dot
                drawCircle(
                    color = VisionTeal.copy(alpha = 0.6f),
                    radius = 3.dp.toPx(),
                    center = Offset(w * 0.5f, midY)
                )
            }
        }

        DrillType.GO_NO_GO -> {
            // Motor Inhibition: Green Go pulse vs Red No-Go barrier
            val cyclePhase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "gonogo_cycle"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.5f)
                val isGo = cyclePhase < 1.2f

                if (isGo) {
                    // Green GO: Radiant expanding circle
                    val pulseRadius = w * 0.28f * (0.8f + (cyclePhase / 1.2f) * 0.25f)
                    drawCircle(
                        color = SportGreen.copy(alpha = 0.25f),
                        radius = pulseRadius * 1.3f,
                        center = center
                    )
                    drawCircle(
                        color = SportGreen,
                        radius = pulseRadius,
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawCircle(
                        color = SportGreen,
                        radius = 4.dp.toPx(),
                        center = center
                    )
                } else {
                    // Amber NO-GO: Warning ring + stop bar
                    val baseRadius = w * 0.28f
                    drawCircle(
                        color = SignalAmber,
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawLine(
                        color = SignalAmber,
                        start = Offset(center.x - baseRadius * 0.6f, center.y),
                        end = Offset(center.x + baseRadius * 0.6f, center.y),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        DrillType.CNS_TAP -> {
            // 10s CNS Tap Cadence: Rapid tapping shockwaves
            val tapPulse by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "cns_tap_pulse"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.5f)

                // Expanding shockwave ripple
                val waveRadius = (w * 0.15f) + (w * 0.28f) * tapPulse
                val waveAlpha = (1f - tapPulse) * 0.6f
                drawCircle(
                    color = VisionTeal.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Central tap pad with impact scale
                val coreRadius = (w * 0.14f) * (1f - tapPulse * 0.15f)
                drawCircle(
                    color = VisionTeal.copy(alpha = 0.2f),
                    radius = coreRadius * 1.5f,
                    center = center
                )
                drawCircle(
                    color = VisionTeal,
                    radius = coreRadius,
                    center = center
                )
            }
        }

        DrillType.TACTILE -> {
            // Tactile Vibration: Haptic concentric shockwave bursts
            val hapticPulse by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "tactile_pulse"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.5f)

                // Concentric vibration shockwaves
                for (ring in 1..3) {
                    val progress = (hapticPulse + (ring * 0.25f)) % 1f
                    val ringRadius = (w * 0.12f) + (w * 0.32f) * progress
                    val ringAlpha = (1f - progress) * 0.7f
                    drawCircle(
                        color = SignalAmber.copy(alpha = ringAlpha),
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Center device icon / haptic motor core
                drawCircle(
                    color = SignalAmber.copy(alpha = 0.25f),
                    radius = w * 0.14f,
                    center = center
                )
                drawCircle(
                    color = SignalAmber,
                    radius = w * 0.08f,
                    center = center
                )
            }
        }

        DrillType.F1_LIGHTS -> {
            // Formula 1 Gantry 5-Light Sequence
            val lightProgress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "f1_lights_anim"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val gantryY = h * 0.5f
                val spacing = w / 6f

                // Draw 5 F1 start lights
                val currentLitCount = lightProgress.toInt().coerceIn(0, 5)
                val isLightsOut = lightProgress >= 5.0f

                for (i in 1..5) {
                    val cx = spacing * i
                    val isRedLit = !isLightsOut && i <= currentLitCount
                    val lightColor = if (isLightsOut) {
                        SportGreen
                    } else if (isRedLit) {
                        Color(0xFFFF2A2A)
                    } else {
                        Color(0xFF2B2B2B)
                    }

                    // Background housing socket
                    drawCircle(
                        color = Color(0xFF161616),
                        radius = w * 0.07f,
                        center = Offset(cx, gantryY)
                    )

                    // Active bulb
                    drawCircle(
                        color = lightColor,
                        radius = w * 0.052f,
                        center = Offset(cx, gantryY)
                    )

                    // Glow if active
                    if (isRedLit || isLightsOut) {
                        drawCircle(
                            color = lightColor.copy(alpha = 0.35f),
                            radius = w * 0.08f,
                            center = Offset(cx, gantryY)
                        )
                    }
                }
            }
        }

        DrillType.STROOP -> {
            // Stroop Color-Word Interference conflict pulse
            val stroopPhase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "stroop_conflict"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.5f)

                val phase = stroopPhase.toInt() % 4
                val activeColor = when (phase) {
                    0 -> Color(0xFFFF4D4D) // Red
                    1 -> VisionTeal        // Cyan/Green
                    2 -> Color(0xFF388BFD) // Blue
                    else -> SignalAmber    // Amber
                }

                // Surrounding conflict ring
                drawCircle(
                    color = activeColor.copy(alpha = 0.25f),
                    radius = w * 0.32f,
                    center = center
                )
                drawCircle(
                    color = activeColor,
                    radius = w * 0.22f,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Center conflict target dot
                drawCircle(
                    color = activeColor,
                    radius = w * 0.1f,
                    center = center
                )
            }
        }

        DrillType.EVEN_ODD -> {
            // High-speed numerical parity flip
            val numberPulse by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "even_odd_pulse"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w * 0.5f, h * 0.5f)

                // Split decision halves (Left: Even, Right: Odd)
                drawRoundRect(
                    color = VisionTeal.copy(alpha = 0.2f),
                    topLeft = Offset(w * 0.12f, h * 0.28f),
                    size = Size(w * 0.34f, h * 0.44f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
                drawRoundRect(
                    color = SignalAmber.copy(alpha = 0.2f),
                    topLeft = Offset(w * 0.54f, h * 0.28f),
                    size = Size(w * 0.34f, h * 0.44f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )

                // Highlighting pulse indicator
                val targetCenter = if (numberPulse > 0.5f) Offset(w * 0.29f, center.y) else Offset(w * 0.71f, center.y)
                drawCircle(
                    color = if (numberPulse > 0.5f) VisionTeal else SignalAmber,
                    radius = 4.dp.toPx(),
                    center = targetCenter
                )
            }
        }

        DrillType.FLANKER -> {
            // Eriksen Flanker: 5 arrows with center focus
            val flankerAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "flanker_arrows"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerY = h * 0.5f
                val spacing = w * 0.15f
                val isCenterRight = flankerAnim > 0.5f

                // Draw 5 arrow vectors (Left Flankers, Center Arrow, Right Flankers)
                for (i in -2..2) {
                    val cx = (w * 0.5f) + (i * spacing)
                    val isCenter = (i == 0)
                    val arrowPointsRight = if (isCenter) isCenterRight else !isCenterRight // Incongruent flankers
                    val color = if (isCenter) VisionTeal else VisionTeal.copy(alpha = 0.35f)
                    val strokeW = if (isCenter) 2.5.dp.toPx() else 1.5.dp.toPx()

                    val dir = if (arrowPointsRight) 1f else -1f
                    val arrowHalfW = spacing * 0.28f

                    drawLine(
                        color = color,
                        start = Offset(cx - arrowHalfW * dir, centerY - arrowHalfW),
                        end = Offset(cx + arrowHalfW * dir, centerY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = color,
                        start = Offset(cx - arrowHalfW * dir, centerY + arrowHalfW),
                        end = Offset(cx + arrowHalfW * dir, centerY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        DrillType.RHYTHM_SYNC -> {
            // Coincidence Anticipation: Sweeping cursor arriving at target baseline
            val syncProgress by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1300, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rhythm_sync_sweep"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val targetBaselineY = h * 0.78f

                // Fixed target baseline
                drawLine(
                    color = VisionTeal,
                    start = Offset(w * 0.15f, targetBaselineY),
                    end = Offset(w * 0.85f, targetBaselineY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Descending precision cursor
                val cursorY = (h * 0.2f) + (targetBaselineY - (h * 0.2f)) * syncProgress
                val isAtBaseline = syncProgress > 0.92f

                drawCircle(
                    color = if (isAtBaseline) SportGreen else VisionTeal,
                    radius = (w * 0.07f),
                    center = Offset(w * 0.5f, cursorY)
                )

                if (isAtBaseline) {
                    drawCircle(
                        color = SportGreen.copy(alpha = 0.4f),
                        radius = (w * 0.12f),
                        center = Offset(w * 0.5f, targetBaselineY)
                    )
                }
            }
        }

        DrillType.CHOICE_4WAY -> {
            // 4 Cardinal Direction Arrows: sequential pulse through Up, Right, Down, Left
            val arrowAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "choice_4way_arrow"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val activeIndex = arrowAnim.toInt() % 4
                val center = Offset(w * 0.5f, h * 0.5f)
                val dist = w * 0.28f

                // 4 arrows: 0 = UP, 1 = RIGHT, 2 = DOWN, 3 = LEFT
                val offsets = listOf(
                    Offset(center.x, center.y - dist),
                    Offset(center.x + dist, center.y),
                    Offset(center.x, center.y + dist),
                    Offset(center.x - dist, center.y)
                )

                offsets.forEachIndexed { idx, pt ->
                    val isActive = idx == activeIndex
                    val col = if (isActive) BrandAccent else VisionTeal.copy(alpha = 0.3f)
                    val rad = if (isActive) (w * 0.11f) else (w * 0.08f)
                    drawCircle(color = col, radius = rad, center = pt)
                }

                // Center indicator dot
                drawCircle(color = VisionTeal.copy(alpha = 0.6f), radius = w * 0.04f, center = center)
            }
        }

        DrillType.COLOR_MATCH -> {
            // Color Matching Choice: 4 colored squares in 2x2 layout, one highlighted in rotation
            val colorPhase by transition.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "color_match_phase"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val activeIdx = colorPhase.toInt() % 4
                val colors = listOf(CoralWarning, CoolBlue, SportGreen, SignalAmber)
                val pad = w * 0.12f
                val cellSize = w * 0.32f

                val positions = listOf(
                    Offset(pad, pad),
                    Offset(w - pad - cellSize, pad),
                    Offset(pad, h - pad - cellSize),
                    Offset(w - pad - cellSize, h - pad - cellSize)
                )

                positions.forEachIndexed { i, pos ->
                    val isActive = i == activeIdx
                    drawRoundRect(
                        color = if (isActive) colors[i] else colors[i].copy(alpha = 0.35f),
                        topLeft = pos,
                        size = Size(cellSize, cellSize),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }

        DrillType.GRID_TRACKING -> {
            // 4x4 Grid Matrix Tracking: 16 cells with target pulse traveling across the grid
            val gridCellAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "grid_tracking_anim"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val activeCell = gridCellAnim.toInt() % 16
                val pad = w * 0.08f
                val cellSize = (w - pad * 2f) / 4.4f
                val spacing = (w - pad * 2f - cellSize * 4f) / 3f

                for (row in 0..3) {
                    for (col in 0..3) {
                        val index = row * 4 + col
                        val x = pad + col * (cellSize + spacing)
                        val y = pad + row * (cellSize + spacing)
                        val isActive = index == activeCell
                        drawRoundRect(
                            color = if (isActive) BrandAccent else VisionTeal.copy(alpha = 0.2f),
                            topLeft = Offset(x, y),
                            size = Size(cellSize, cellSize),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }
        }

        DrillType.SPATIAL_AUDIO -> {
            // Binaural earbud acoustic impulse: Left ear waves, then Right ear waves
            val earWave by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "spatial_audio_wave"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val isLeftActive = earWave < 0.5f
                val waveProg = if (isLeftActive) earWave * 2f else (earWave - 0.5f) * 2f

                // Left earbud representation
                val leftCenter = Offset(w * 0.25f, h * 0.5f)
                drawCircle(
                    color = if (isLeftActive) VisionTeal else TextMuted.copy(alpha = 0.3f),
                    radius = w * 0.1f,
                    center = leftCenter
                )

                // Right earbud representation
                val rightCenter = Offset(w * 0.75f, h * 0.5f)
                drawCircle(
                    color = if (!isLeftActive) VisionTeal else TextMuted.copy(alpha = 0.3f),
                    radius = w * 0.1f,
                    center = rightCenter
                )

                // Acoustic sound wave radiating from active earbud
                val activeCenter = if (isLeftActive) leftCenter else rightCenter
                drawCircle(
                    color = VisionTeal.copy(alpha = 1f - waveProg),
                    radius = (w * 0.12f) + (w * 0.16f * waveProg),
                    center = activeCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        DrillType.QUADRANT_CHOICE -> {
            // 4-Quadrant Flashing Choice: 2x2 quadrants where one quadrant flashes
            val quadrantAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "quadrant_anim"
            )

            Canvas(modifier = modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val activeQuad = quadrantAnim.toInt() % 4
                val halfW = (w - 6.dp.toPx()) / 2f
                val halfH = (h - 6.dp.toPx()) / 2f

                val coords = listOf(
                    Offset(0f, 0f),
                    Offset(w - halfW, 0f),
                    Offset(0f, h - halfH),
                    Offset(w - halfW, h - halfH)
                )

                coords.forEachIndexed { i, pt ->
                    val isActive = i == activeQuad
                    drawRoundRect(
                        color = if (isActive) VisionTeal else CharcoalCardElevated,
                        topLeft = pt,
                        size = Size(halfW, halfH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }
    }
}
