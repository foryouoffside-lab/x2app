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
    val accuracyPercent: Int,
    val consistencyMs: Long,
    val isVerified: Boolean = true,
    val note: String = ""
)
