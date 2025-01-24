package com.example.GardenTracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.GardenTracker.model.Memory

@Database(
    entities = [Memory::class],
    version = 1,
    exportSchema = false
)
abstract class MemoryDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao

    companion object {

        private const val DATABASE_NAME : String = "memory-database"

        @Volatile private var INSTANCE: MemoryDatabase? = null

        fun getInstance(context: Context): MemoryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                MemoryDatabase::class.java, DATABASE_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Populate database on new thread
                    }
                }).build()

    }

}