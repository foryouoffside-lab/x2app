package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SessionEntity::class], version = 3, exportSchema = false)
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
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
