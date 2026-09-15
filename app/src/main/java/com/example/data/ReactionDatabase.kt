package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SessionEntity::class], version = 2, exportSchema = false)
abstract class ReactionDatabase : RoomDatabase() {
    abstract fun reactionDao(): ReactionDao

    companion object {
        @Volatile
        private var INSTANCE: ReactionDatabase? = null

        fun getInstance(context: Context): ReactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReactionDatabase::class.java,
                    "reaction_database.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed coach-approved athletic session records
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).reactionDao()
                            val now = System.currentTimeMillis()
                            val oneDay = 86_400_000L
                            dao.insertSession(
                                SessionEntity(
                                    drillId = "go_no_go",
                                    drillTitle = "Inhibition (Go / No-Go)",
                                    timestamp = now - 1_800_000L,
                                    medianTimeMs = 268,
                                    bestTimeMs = 242,
                                    accuracyPercent = 95,
                                    consistencyMs = 15,
                                    cvPercent = 5.6f,
                                    falseStarts = 0,
                                    athleteName = "Alex Morgan",
                                    sportCategory = "Motorsport",
                                    rawTrialsCsv = "274,261,288,242,268",
                                    note = "Superb impulse suppression. 0 commission errors on red distractors."
                                )
                            )
                            dao.insertSession(
                                SessionEntity(
                                    drillId = "auditory",
                                    drillTitle = "Acoustic Reflex",
                                    timestamp = now - 7_200_000L,
                                    medianTimeMs = 158,
                                    bestTimeMs = 144,
                                    accuracyPercent = 100,
                                    consistencyMs = 11,
                                    cvPercent = 6.9f,
                                    falseStarts = 0,
                                    athleteName = "Alex Morgan",
                                    sportCategory = "Motorsport",
                                    rawTrialsCsv = "162,154,144,168,158",
                                    note = "Elite brainstem auditory conduction. Sub-160ms starter reflex."
                                )
                            )
                            dao.insertSession(
                                SessionEntity(
                                    drillId = "cns_tap",
                                    drillTitle = "10s CNS Tap Test",
                                    timestamp = now - 28_800_000L,
                                    medianTimeMs = 132,
                                    bestTimeMs = 118,
                                    accuracyPercent = 100,
                                    consistencyMs = 9,
                                    cvPercent = 6.8f,
                                    falseStarts = 0,
                                    athleteName = "Alex Morgan",
                                    sportCategory = "Motorsport",
                                    rawTrialsCsv = "76 taps in 10s (7.6 Hz)",
                                    note = "Central Nervous System readiness optimal. Low motor fatigue."
                                )
                            )
                            dao.insertSession(
                                SessionEntity(
                                    drillId = "choice",
                                    drillTitle = "Choice Reaction (CRT)",
                                    timestamp = now - oneDay,
                                    medianTimeMs = 236,
                                    bestTimeMs = 220,
                                    accuracyPercent = 92,
                                    consistencyMs = 18,
                                    cvPercent = 7.6f,
                                    falseStarts = 0,
                                    athleteName = "Alex Morgan",
                                    sportCategory = "Motorsport",
                                    rawTrialsCsv = "240,220,248,236,236",
                                    note = "Fast visual response. Train 4-choice decision speed next."
                                )
                            )
                            dao.insertSession(
                                SessionEntity(
                                    drillId = "classic",
                                    drillTitle = "Visual Reaction (SRT)",
                                    timestamp = now - (2 * oneDay),
                                    medianTimeMs = 214,
                                    bestTimeMs = 208,
                                    accuracyPercent = 100,
                                    consistencyMs = 14,
                                    cvPercent = 6.5f,
                                    falseStarts = 0,
                                    athleteName = "Alex Morgan",
                                    sportCategory = "Motorsport",
                                    rawTrialsCsv = "218,214,208,222,214",
                                    note = "All-time personal record achieved under verified calibration."
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
