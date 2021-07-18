package com.example.GardenTracker.database


import androidx.room.*
import com.example.GardenTracker.model.Note

@Dao
interface NoteDao {
    @Query("SELECT COUNT(id) FROM note")
    fun getNumNotes(): Int

    @Query("SELECT * FROM note")
    fun getSavedNotes(): List<Note>

    @Query("SELECT * FROM note WHERE id == (:noteID)")
    fun getNoteByUid(noteID: Int) : Note

    @Query("SELECT * FROM note WHERE crop_name == (:name)")
    fun getNotesByCrop(name: String) : List<Note>

    @Query("SELECT * FROM note WHERE crop_type == (:type)")
    fun getNotesByType(type: String) : List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOne(vararg newNote: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(newNotes: List<Note>)

    @Update
    fun updateNote(note: Note)

    @Update // ???
    fun updateNotes(notes: List<Note>)

    @Delete
    fun deleteNote(delNote: Note)

    @Query("DELETE FROM note")
    fun nukeNotes()
}