package com.example.GardenTracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.GardenTracker.model.Crop

// See: https://gist.github.com/florina-muntenescu/697e543652b03d3d2a06703f5d6b44b5

@Database(
    entities = [Crop::class],
    version = 6,
    exportSchema = false
)
abstract class GardenDatabase() : RoomDatabase() {

    abstract fun cropDao(): CropDao

    companion object {

        private const val DATABASE_NAME : String = "garden-database"

        @Volatile private var INSTANCE: GardenDatabase? = null

        fun getInstance(context: Context): GardenDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                GardenDatabase::class.java, DATABASE_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Populate database on new thread
                    }
                }).fallbackToDestructiveMigration().build()

    }
}