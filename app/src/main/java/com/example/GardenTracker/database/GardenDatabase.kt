package com.example.GardenTracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.GardenTracker.model.Crop
import com.example.GardenTracker.model.Memory
import com.example.GardenTracker.model.Note

@Database(
    entities = [Crop::class, Memory::class, Note::class],
    version = 7, // Increment version
    exportSchema = false
)
abstract class GardenDatabase : RoomDatabase() {

    abstract fun cropDao(): CropDao
    abstract fun memoryDao(): MemoryDao
    abstract fun noteDao(): NoteDao

    companion object {
        private const val DATABASE_NAME: String = "garden-database"

        @Volatile
        private var INSTANCE: GardenDatabase? = null

        fun getInstance(context: Context): GardenDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                GardenDatabase::class.java, DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
