package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {
    @Query("SELECT * FROM drill_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM drill_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<SessionEntity>>

    @Query("SELECT MIN(medianTimeMs) FROM drill_sessions WHERE drillId = 'classic' AND medianTimeMs > 0")
    suspend fun getBestClassicMs(): Long?

    @Query("SELECT COUNT(*) FROM drill_sessions")
    suspend fun getSessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long
}
