package com.example.resqprototype.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SosAlertEntity::class], version = 1, exportSchema = false)
abstract class ResqDatabase : RoomDatabase() {

    abstract fun sosAlertDao(): SosAlertDao

    companion object {
        @Volatile
        private var INSTANCE: ResqDatabase? = null

        fun getInstance(context: Context): ResqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ResqDatabase::class.java,
                    "resq_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
