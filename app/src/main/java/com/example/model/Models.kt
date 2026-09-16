package com.example.model

enum class TaskComplexity(
    val id: String,
    val title: String,
    val code: String,
    val description: String,
    val typicalBenchmark: String
) {
    SRT("srt", "Simple Reaction Time", "SRT", "One stimulus requiring one fixed response (e.g. launching at starting gun)", "140–250 ms"),
    CRT("crt", "Choice Reaction Time", "CRT", "Multiple different stimuli, each requiring a specific response", "350–500+ ms"),
    RRT("rrt", "Recognition Reaction Time", "RRT", "Respond only to specific target and ignore distractors (Go/No-Go)", "200–350 ms")
}

enum class SensoryInput(
    val id: String,
    val title: String,
    val description: String
) {
    AUDITORY("auditory", "Auditory Speed", "Triggered by sound waves; fastest natural conduction pathway"),
    VISUAL("visual", "Visual Speed", "Triggered by sight or light changes in retina"),
    TACTILE("tactile", "Tactile Speed", "Triggered by physical touch, pressure, or vibration"),
    KINETIC("kinetic", "Neuromuscular Cadence", "Triggered by internal rhythm, cadence, or motor output")
}

enum class DrillCategory(
    val id: String,
    val title: String,
    val shortName: String,
    val description: String,
    val neuralPathway: String
) {
    SENSORY("sensory", "Sensory Reflex", "Sensory", "Raw stimulus-to-motor latency bypassing cognitive deliberation.", "Retino-Cochlear -> Brainstem -> Motor Strip"),
    COGNITIVE("cognitive", "Cognitive & Decision", "Decision", "Information processing speed, stimulus discrimination, and mental choice.", "Occipito-Parietal -> Prefrontal Cortex -> M1"),
    INHIBITION("inhibition", "Motor Inhibition & Control", "Inhibition", "Prefrontal impulse suppression and false-start commission avoidance.", "Dorsolateral PFC -> Subthalamic Nucleus"),
    VISUAL("visual", "Visual & Peripheral Field", "Visual", "Saccadic eye-steering, Useful Field of View (UFOV), and spatial acquisition.", "Superior Colliculus -> Frontal Eye Fields"),
    NEUROMUSCULAR("neuromuscular", "Neuromuscular & Rhythm", "Motor", "Maximum central nervous system motor unit firing rate and temporal anticipation.", "Corticospinal Tract -> High-Frequency Motor Units")
}

enum class DrillType(val id: String, val title: String, val category: String) {
    CLASSIC("classic", "Visual Reflex", "Sensory Reflex"),
    AUDITORY("auditory", "Acoustic Reflex", "Sensory Reflex"),
    TACTILE("tactile", "Tactile Vibration", "Sensory Reflex"),
    F1_LIGHTS("f1_lights", "F1 Start Lights", "Sensory Reflex"),
    CHOICE("choice", "Directional Choice", "Cognitive Decision"),
    STROOP("stroop", "Stroop Color Conflict", "Cognitive Decision"),
    EVEN_ODD("even_odd", "Numerical Parity", "Cognitive Decision"),
    GO_NO_GO("go_no_go", "Motor Inhibition", "Motor Inhibition"),
    FLANKER("flanker", "Flanker Attention", "Motor Inhibition"),
    FLASH_GRID("flash_grid", "Peripheral Flash Grid", "Visual & Peripheral"),
    PRECISION("precision", "Saccadic Precision", "Visual & Peripheral"),
    CNS_TAP("cns_tap", "10s CNS Tap Test", "Neuromuscular"),
    RHYTHM_SYNC("rhythm_sync", "Anticipation Timing", "Neuromuscular"),
    CHOICE_4WAY("choice_4way", "4-Way Arrow Vectors", "Cognitive Decision"),
    COLOR_MATCH("color_match", "Color Matching Choice", "Cognitive Decision"),
    GRID_TRACKING("grid_tracking", "4x4 Grid Matrix Tracking", "Cognitive Decision"),
    SPATIAL_AUDIO("spatial_audio", "Spatial Audio Earbud Vector", "Cognitive Decision"),
    QUADRANT_CHOICE("quadrant_choice", "4-Quadrant Flashing Choice", "Cognitive Decision")
}

enum class DrillState {
    STANDBY,
    WAITING_FOR_STIMULUS,
    STIMULUS_ACTIVE,
    TOO_SOON,
    FALSE_START,
    TRIAL_FEEDBACK,
    PAUSED,
    FINISHED
}

fun drillTypeFromId(id: String): DrillType? = DrillType.values().find { it.id == id }

// Scored trials per Test run.
//
// Five was far too few to call a benchmark: the test-retest reliability of a 5-trial
// median is poor, and the app states targets to the millisecond. Published SRT/CRT
// protocols use 20-40 scored trials, so that is what a Test now runs.
val DrillType.scoredTrialsCount: Int
    get() = when (this) {
        // A fixed 10-second tapping burst; trial count does not apply.
        DrillType.CNS_TAP -> 1
        // Needs enough No-Go trials to measure inhibition: 24 at 25% No-Go gives 6.
        DrillType.GO_NO_GO -> 24
        // Anticipation timing is slow and fatiguing; fewer trials, still enough to average.
        DrillType.RHYTHM_SYNC -> 12
        else -> 20
    }

// Unscored practice trials at the start of every Test run.
//
// The first trial of a run is reliably slower - the hand is not set and the layout is
// unfamiliar - and at small n that bias lands squarely in the median. These run for real
// but are excluded from every statistic.
val DrillType.warmUpTrialsCount: Int
    get() = when (this) {
        DrillType.CNS_TAP -> 0
        else -> 2
    }

// Proportion of trials where no stimulus ever appears.
//
// Without catch trials the only guard against guessing is the <100ms rule, which catches
// only egregious jumps: someone tapping a steady rhythm looks identical to someone fast.
// A catch trial has no stimulus, so any response on it is a pure false alarm.
val DrillType.catchTrialRate: Float
    get() = when (this) {
        // These score withholding or timing directly; a catch trial would double up.
        DrillType.CNS_TAP, DrillType.GO_NO_GO, DrillType.RHYTHM_SYNC -> 0f
        else -> 0.12f
    }

// Total trials presented in a Test run, practice and catch trials included.
val DrillType.totalTrialsCount: Int
    get() {
        if (this == DrillType.CNS_TAP) return 1
        val scored = scoredTrialsCount + warmUpTrialsCount
        val catches = kotlin.math.ceil(scored * catchTrialRate).toInt()
        return scored + catches
    }

val DrillType.isChoiceCategory: Boolean
    get() = category == "Cognitive Decision"

data class DrillInfo(
    val type: DrillType,
    val subtitle: String,
    val badge: String,
    val purpose: String,
    val protocolDetails: String = "",
    val defaultDuration: String = "2 min",
    val categoryEnum: DrillCategory = DrillCategory.SENSORY,
    val complexity: TaskComplexity = TaskComplexity.SRT,
    val sensoryInput: SensoryInput = SensoryInput.VISUAL,
    val targetBenchmark: String = "< 200 ms",
    val difficulty: String = "Intermediate",
    val levelNumber: Int = 1
)

data class TrialRecord(
    val trialIndex: Int,
    val latencyMs: Long,
    val isCorrect: Boolean,
    val isFalseStart: Boolean, // < 100ms Olympic false start violation
    val stimulusInfo: String,
    val timestamp: Long = System.currentTimeMillis(),
    // Practice trial: presented and played for real, but excluded from every statistic.
    val isWarmUp: Boolean = false,
    // No stimulus was ever shown. Responding here is a false alarm; withholding is correct.
    val isCatchTrial: Boolean = false
)

data class DrillRunResult(
    val drillType: DrillType,
    val medianTimeMs: Long,
    val bestTimeMs: Long,
    val accuracyPercent: Int,
    val consistencyMs: Long,
    val cvPercent: Float,          // Coefficient of Variation: (StdDev / Mean * 100)
    val falseStarts: Int,          // IAAF <100ms anticipation violations
    val inverseEfficiencyScore: Long, // IES = RT / Accuracy
    val exGaussianTau: Long,       // Attentional lapse tail approximation ms
    val cnsFrequencyHz: Float? = null, // Central nervous system tapping rate
    val vsBaselinePercent: Float,
    val isPersonalBest: Boolean,
    val coachNote: String,
    val isVerified: Boolean = true,
    val athleteName: String = "",
    val rawTrials: List<TrialRecord> = emptyList(),
    val mode: DrillMode = DrillMode.TEST,
    val survivedSec: Int = 0,
    val levelReached: Int = 0
)


enum class BatteryProtocolType {
    SINGLE_TYPE_SPECIALIZED, // Concept 1: Specialized CRT Battery (Average of Medians across multiple layouts)
    MULTI_TYPE_MATRIX        // Concept 2: Multi-Type Matrix (Normalized composite Reaction Index)
}

data class BatteryDrillScore(
    val drillType: DrillType,
    val medianMs: Long = 0L,
    val bestMs: Long = 0L,
    val accuracyPercent: Int = 100,
    val consistencyMs: Long = 0L,
    val isCompleted: Boolean = false,
    val falseStarts: Int = 0,
    val normalizedScore: Int = 0
)

data class BatteryAssessmentResult(
    val batteryId: String,
    val batteryTitle: String,
    val protocolType: BatteryProtocolType,
    val athleteName: String,
    val drillScores: List<BatteryDrillScore>,
    val finalScoreValue: Float, // ms for Single-Type, 0-100 score for Multi-Type
    val finalScoreUnit: String, // "ms" or "/ 100"
    val rankTitle: String,
    val analysisSummary: String,
    val calculationFormula: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SportsBattery(
    val id: String,
    val title: String,
    val sportTag: String,
    val description: String,
    val drills: List<DrillType>,
    val durationMin: String,
    val benchmark: String,
    val protocolType: BatteryProtocolType = BatteryProtocolType.SINGLE_TYPE_SPECIALIZED,
    val baselineNormMs: Map<DrillType, Long> = emptyMap()
)


data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    var isUnread: Boolean = true
)

data class UserProfile(
    val name: String = "Athlete",
    val handle: String = "",
    val country: String = "",
    val countryFlag: String = "",
    val sport: String = "Reaction Training",
    val rpi: Int = 0,
    val weeklyDelta: String = "",
    val streakDays: Int = 0,
    val baselineMs: Long = 0,
    val fastestMs: Long = 0,
    val totalSessions: Int = 0,
    val memberSince: String = ""
)

data class PerformanceMetric(
    val name: String,
    val score: Int,
    val detailValue: String,
    val trendDescription: String,
    val description: String
)

