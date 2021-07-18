package com.example.GardenTracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.GardenTracker.model.Note

// See: https://gist.github.com/florina-muntenescu/697e543652b03d3d2a06703f5d6b44b5

@Database(entities = arrayOf(Note::class), version = 1, exportSchema = false)
abstract class NoteDatabase() : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {

        val DATABASE_NAME : String = "note-database"

        @Volatile private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                NoteDatabase::class.java, DATABASE_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Populate database on new thread
                    }
                }).build()

    }
}