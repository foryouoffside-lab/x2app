package com.example.model

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * How a drill is run.
 *
 * The two modes exist because measuring reaction time and training it want opposite
 * things from a drill, and one format cannot do both honestly:
 *
 * - [TEST] is the instrument. A fixed number of trials, no difficulty ramp, no clock,
 *   no carry-over from previous runs. Every Test session is directly comparable to
 *   every other one, which is what makes a median, a baseline and a trend mean
 *   anything. Only Test sessions feed the Progress trend and the benchmarks.
 *
 * - [TRAIN] is the game. A countdown clock is the only fail state, correct responses
 *   buy time, mistakes cost it, and difficulty ramps with level so the run ends when
 *   you can no longer keep up. Survival time and level reached are the score.
 *   Reaction times from a Train run are NOT comparable across sessions, because the
 *   difficulty at any moment depends on how far you got, so they never touch the trend.
 */
enum class DrillMode {
    TEST,
    TRAIN
}

/**
 * Shared rules for [DrillMode.TRAIN] runs.
 *
 * The model: the clock is the only way to lose. A correct response adds
 * [rewardForActionRate] seconds, a mistake subtracts [MISTAKE_PENALTY_SEC], and the
 * clock cannot bank more than it started with. Difficulty keys off *level*, never off
 * elapsed time — with a refillable clock, a time-based ramp runs backwards, because
 * playing well raises the remaining time and would make the drill easier.
 */
object TrainingRules {

    /** Starting clock for a solo run, in seconds. */
    const val TOTAL_TIME_SEC = 45f

    /** The clock can be refilled by good play, but never above where it started. */
    const val CLOCK_CEILING_SEC = TOTAL_TIME_SEC

    /** Seconds removed by a wrong response, a false start or a missed window. */
    const val MISTAKE_PENALTY_SEC = 1.0f

    /** Correct responses needed per level for most drills. */
    const val HITS_PER_LEVEL_DEFAULT = 4

    /** Level at which the ramp has effectively reached its floor. */
    const val MAX_LEVEL = 40

    /** Accuracy a competent athlete is expected to hold; the clock is tuned to break even here. */
    const val TARGET_ACCURACY = 0.8f

    /**
     * Seconds a correct response must buy for the clock to break even at
     * [TARGET_ACCURACY], given how often this drill lets you act.
     *
     * The clock drains 1s per second. Over one second a drill firing [actionsPerSec]
     * stimuli yields `actionsPerSec * accuracy` hits and `actionsPerSec * (1 - accuracy)`
     * mistakes, so breaking even requires:
     *
     *     actionsPerSec * accuracy * reward  =  1 + actionsPerSec * (1 - accuracy) * penalty
     *
     * Without this, a slow drill can never refill the clock at any accuracy: every run
     * lasts exactly [TOTAL_TIME_SEC] and skill cannot show in the score.
     */
    fun rewardForActionRate(actionsPerSec: Float, accuracy: Float = TARGET_ACCURACY): Float {
        if (actionsPerSec <= 0f || accuracy <= 0f) return 0f
        return (1f + actionsPerSec * (1f - accuracy) * MISTAKE_PENALTY_SEC) / (actionsPerSec * accuracy)
    }

    /** Level reached after [hits] correct responses, 1-based and capped at [MAX_LEVEL]. */
    fun levelForHits(hits: Int, hitsPerLevel: Int = HITS_PER_LEVEL_DEFAULT): Int {
        if (hitsPerLevel <= 0) return 1
        return min(MAX_LEVEL, (hits / hitsPerLevel) + 1)
    }

    /** Clock after a correct response. */
    fun applyHit(timeRemaining: Float, reward: Float): Float =
        min(CLOCK_CEILING_SEC, timeRemaining + reward)

    /** Clock after a mistake. */
    fun applyMistake(timeRemaining: Float): Float =
        max(0f, timeRemaining - MISTAKE_PENALTY_SEC)

    /**
     * A duration that tightens with level: [startMs] at level 1, approaching [floorMs]
     * by [MAX_LEVEL]. The floor is a human limit, not a difficulty dial — pushing a
     * response window below what a person can physically resolve makes a drill
     * unfair rather than hard.
     *
     * Decays geometrically so early levels step down noticeably and late ones taper,
     * which keeps the run survivable deep in.
     */
    fun rampMs(level: Int, startMs: Long, floorMs: Long): Long {
        val steps = (level.coerceIn(1, MAX_LEVEL) - 1).toFloat()
        val span = (startMs - floorMs).toFloat()
        return (floorMs + span * RAMP_DECAY.pow(steps)).toLong().coerceAtLeast(floorMs)
    }

    /** A value that grows with level from [start] toward [ceiling]. */
    fun rampUp(level: Int, start: Float, ceiling: Float): Float {
        val steps = (level.coerceIn(1, MAX_LEVEL) - 1).toFloat()
        return ceiling - (ceiling - start) * RAMP_DECAY.pow(steps)
    }

    private const val RAMP_DECAY = 0.92f
}

/**
 * Per-drill Train tuning.
 *
 * [actionsPerSec] is how often this drill gives you something to respond to; it sets
 * the time a hit is worth. [windowStartMs]/[windowFloorMs] bound the response window.
 */
data class TrainTuning(
    val actionsPerSec: Float,
    val windowStartMs: Long,
    val windowFloorMs: Long,
    val hitsPerLevel: Int = TrainingRules.HITS_PER_LEVEL_DEFAULT
) {
    val timePerHitSec: Float get() = TrainingRules.rewardForActionRate(actionsPerSec)
    fun windowMsForLevel(level: Int): Long =
        TrainingRules.rampMs(level, windowStartMs, windowFloorMs)
}

/**
 * Train tuning per drill.
 *
 * Floors are per-paradigm human limits: simple visual/auditory reflex resolves far
 * faster than a conflict task, so a Flanker window must not be squeezed to a Visual
 * Reflex floor.
 */
val DrillType.trainTuning: TrainTuning
    get() = when (this) {
        // Simple reflex: one stimulus, one response. Fast floor.
        DrillType.CLASSIC -> TrainTuning(1.0f, 900L, 260L)
        DrillType.AUDITORY -> TrainTuning(1.0f, 900L, 240L)
        DrillType.TACTILE -> TrainTuning(1.0f, 900L, 260L)
        DrillType.F1_LIGHTS -> TrainTuning(0.5f, 1200L, 320L, hitsPerLevel = 2)
        DrillType.FLASH_GRID -> TrainTuning(0.8f, 1100L, 340L)
        DrillType.PRECISION -> TrainTuning(0.8f, 1200L, 380L)

        // Choice reaction: stimulus must be discriminated before responding.
        DrillType.CHOICE -> TrainTuning(0.9f, 1100L, 360L)
        DrillType.CHOICE_4WAY -> TrainTuning(0.8f, 1250L, 420L)
        DrillType.QUADRANT_CHOICE -> TrainTuning(0.8f, 1250L, 420L)
        DrillType.COLOR_MATCH -> TrainTuning(0.8f, 1250L, 420L)
        DrillType.GRID_TRACKING -> TrainTuning(0.7f, 1400L, 460L)
        DrillType.SPATIAL_AUDIO -> TrainTuning(0.7f, 1400L, 440L)

        // Interference and inhibition: slowest floors, these cannot be rushed.
        DrillType.STROOP -> TrainTuning(0.8f, 1400L, 450L)
        DrillType.FLANKER -> TrainTuning(0.8f, 1100L, 380L)
        DrillType.EVEN_ODD -> TrainTuning(0.8f, 1300L, 430L)
        DrillType.GO_NO_GO -> TrainTuning(0.8f, 1100L, 400L)

        // Rhythm and motor output.
        DrillType.RHYTHM_SYNC -> TrainTuning(0.6f, 1300L, 400L, hitsPerLevel = 3)
        DrillType.CNS_TAP -> TrainTuning(4.0f, 600L, 200L)
    }

/** CNS Tap is a fixed 10-second rate test; a countdown Train mode would not mean anything. */
val DrillType.supportsTrainMode: Boolean
    get() = this != DrillType.CNS_TAP

/**
 * Longest a Test trial will wait for a response before scoring it a miss.
 *
 * Without a deadline a single distraction records a multi-second "reaction time" that
 * lands in the median and quietly ruins the run. The limits are generous - far beyond any
 * genuine response - so they only ever catch a lapse, never a slow-but-real answer.
 */
val DrillType.responseDeadlineMs: Long
    get() = when (this) {
        // No deadline applies: these are scored by withholding, rate or timing.
        DrillType.CNS_TAP, DrillType.GO_NO_GO, DrillType.RHYTHM_SYNC -> 0L
        // Simple reflex: a real response is well under 500ms.
        DrillType.CLASSIC, DrillType.AUDITORY, DrillType.TACTILE, DrillType.F1_LIGHTS -> 1500L
        // Interference tasks legitimately take longer to resolve.
        DrillType.STROOP, DrillType.FLANKER, DrillType.EVEN_ODD -> 3000L
        // Choice and visual search.
        else -> 2500L
    }
