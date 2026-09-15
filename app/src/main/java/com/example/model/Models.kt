package com.example.model

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
    F1_LIGHTS("f1_lights", "F1 Start Lights", "Sensory Reflex"),
    CHOICE("choice", "Directional Choice", "Cognitive Decision"),
    STROOP("stroop", "Stroop Color Conflict", "Cognitive Decision"),
    EVEN_ODD("even_odd", "Numerical Parity", "Cognitive Decision"),
    GO_NO_GO("go_no_go", "Motor Inhibition", "Motor Inhibition"),
    FLANKER("flanker", "Flanker Attention", "Motor Inhibition"),
    FLASH_GRID("flash_grid", "Peripheral Flash Grid", "Visual & Peripheral"),
    PRECISION("precision", "Saccadic Precision", "Visual & Peripheral"),
    CNS_TAP("cns_tap", "10s CNS Tap Test", "Neuromuscular"),
    RHYTHM_SYNC("rhythm_sync", "Anticipation Timing", "Neuromuscular")
}

data class DrillInfo(
    val type: DrillType,
    val subtitle: String,
    val badge: String,
    val purpose: String,
    val protocolDetails: String = "",
    val defaultDuration: String = "5 min",
    val categoryEnum: DrillCategory = DrillCategory.SENSORY,
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
    val timestamp: Long = System.currentTimeMillis()
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
    val athleteName: String = "Alex Morgan",
    val rawTrials: List<TrialRecord> = emptyList()
)

data class AthleteProfile(
    val id: String,
    val name: String,
    val handle: String,
    val sport: String,
    val role: String,
    val avatarInitial: String,
    val countryFlag: String,
    val rpi: Int,
    val baselineMs: Long,
    val fastestMs: Long,
    val cnsTapBaselineHz: Float = 7.4f,
    val cvPercent: Float = 6.2f,
    val totalSessions: Int,
    val isActive: Boolean = false
)

data class SportsBattery(
    val id: String,
    val title: String,
    val sportTag: String,
    val description: String,
    val drills: List<DrillType>,
    val durationMin: String,
    val benchmark: String
)

data class LeaderboardPlayer(
    val rank: Int,
    val name: String,
    val handle: String,
    val avatarInitial: String,
    val countryFlag: String,
    val scoreText: String,
    val isVerified: Boolean,
    val isCurrentUser: Boolean = false,
    val rpi: Int = 740,
    val bestDrill: String = "Classic Reaction: 208 ms"
)

data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    var isUnread: Boolean = true
)

data class UserProfile(
    val name: String = "Alex Morgan",
    val handle: String = "@alexplays",
    val country: String = "India",
    val countryFlag: String = "🇮🇳",
    val sport: String = "Motorsport / F1 Academy",
    val rpi: Int = 742,
    val rankLabel: String = "Top 18%",
    val weeklyDelta: String = "+24 this week",
    val streakDays: Int = 7,
    val baselineMs: Long = 248,
    val fastestMs: Long = 214,
    val totalSessions: Int = 21,
    val memberSince: String = "Aug 2026"
)

data class PerformanceMetric(
    val name: String,
    val score: Int,
    val detailValue: String,
    val trendDescription: String,
    val description: String
)

