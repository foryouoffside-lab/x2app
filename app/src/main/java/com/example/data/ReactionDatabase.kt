package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SessionEntity::class], version = 1, exportSchema = false)
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
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed prototype records matching V1 specs
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).reactionDao()
                            val now = System.currentTimeMillis()
                            val oneDay = 86_400_000L
                            dao.insertSession(
                                SessionEntity(
                                    drillId = "choice",
                                    drillTitle = "Choice Reaction",
                                    timestamp = now - 3_600_000L,
                                    medianTimeMs = 236,
                                    accuracyPercent = 92,
                                    consistencyMs = 18,
                                    note = "Fast visual response. Train decision speed next."
                                )
                            )
                            dao.insertSession(
                                SessionEntity(
                                    drillId = "flash_grid",
                                    drillTitle = "Flash Grid",
                                    timestamp = now - oneDay,
                                    medianTimeMs = 244,
                                    accuracyPercent = 91,
                                    consistencyMs = 22,
                                    note = "Solid peripheral target acquisition."
                                )
                            )
                            dao.insertSession(
                                SessionEntity(
                                    drillId = "classic",
                                    drillTitle = "Classic Reaction",
                                    timestamp = now - (2 * oneDay),
                                    medianTimeMs = 214,
                                    accuracyPercent = 100,
                                    consistencyMs = 14,
                                    note = "All-time personal record achieved."
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
