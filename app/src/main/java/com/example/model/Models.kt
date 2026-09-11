package com.example.model

enum class DrillType(val id: String, val title: String, val category: String) {
    CLASSIC("classic", "Classic Reaction", "Visual"),
    CHOICE("choice", "Choice Reaction", "Decision"),
    PRECISION("precision", "Precision Tap", "Precision"),
    FLASH_GRID("flash_grid", "Flash Grid", "Peripheral")
}

data class DrillInfo(
    val type: DrillType,
    val subtitle: String,
    val badge: String,
    val purpose: String,
    val defaultDuration: String = "5 min"
)

data class DrillRunResult(
    val drillType: DrillType,
    val medianTimeMs: Long,
    val bestTimeMs: Long,
    val accuracyPercent: Int,
    val consistencyMs: Long,
    val vsBaselinePercent: Float,
    val isPersonalBest: Boolean,
    val coachNote: String,
    val isVerified: Boolean = true
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
