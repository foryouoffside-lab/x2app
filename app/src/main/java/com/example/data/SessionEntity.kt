package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drill_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val drillId: String,
    val drillTitle: String,
    val timestamp: Long,
    val medianTimeMs: Long,
    val bestTimeMs: Long = 0L,
    val accuracyPercent: Int,
    val consistencyMs: Long,
    val cvPercent: Float = 0f,
    val falseStarts: Int = 0,
    val athleteName: String = "",
    val sportCategory: String = "",
    val isVerified: Boolean = true,
    val rawTrialsCsv: String = "",
    val note: String = "",
    // "TEST" = measured protocol, comparable across sessions and the only kind that
    // feeds the trend. "TRAIN" = timed run; its times are not comparable, so they are
    // stored for history but excluded from every benchmark and trend calculation.
    val mode: String = "TEST",
    // Train-only outcome. Zero on Test sessions.
    val survivedSec: Int = 0,
    val levelReached: Int = 0
)

