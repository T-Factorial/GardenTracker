package com.example.GardenTracker.database

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.GardenTracker.executor.ioThread
import com.example.GardenTracker.model.Crop
import com.example.GardenTracker.model.Memory
import com.example.GardenTracker.model.Note
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

// TODO: implement AsyncTasks for remaining methods.

class DatabaseGateway constructor (context: Context) {

    companion object {
        private lateinit var cropDao: CropDao
        private lateinit var noteDao: NoteDao
        private lateinit var memoryDao: MemoryDao
    }

    val TAG = "DATABASE_GATEWAY"

    private var gdb: GardenDatabase = GardenDatabase.getInstance(context)
    private var ndb: NoteDatabase = NoteDatabase.getInstance(context)
    private var mdb: MemoryDatabase = MemoryDatabase.getInstance(context)

    init {
        cropDao = gdb.cropDao()
        noteDao = ndb.noteDao()
        memoryDao = mdb.memoryDao()
        if (gdb.isOpen) {
            Log.d(TAG, "Garden Database open and ready for use.")
        } else {
            Log.e(TAG, "Failed to open Garden Database")
        }
        if (ndb.isOpen) {
            Log.d(TAG, "Note Database open and ready for use.")
        } else {
            Log.e(TAG, "Failed to open Note Database")
        }
        if (mdb.isOpen) {
            Log.d(TAG, "Memory Database open and ready for use.")
        } else {
            Log.e(TAG, "Failed to open Memory Database")
        }
    }

    /*****************************************************
     * Begin Crop Dao
     *****************************************************/

    fun closeCropDb() {
        if (gdb.isOpen) {
            gdb.close()
            Log.d(TAG, "Crop database closed.")
        }
    }

    fun closeNoteDb() {
        if (ndb.isOpen) {
            ndb.close()
            Log.d(TAG, "Note database closed.")
        }
    }

    fun getAllCrops() : ArrayList<Crop> {

        var crops: ArrayList<Crop> = ArrayList()
        Log.d(TAG, "Retrieving crops from database...")
        try {
            crops = GetCropsTask().execute().get().toCollection(ArrayList())
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch(e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
        return crops
    }

    internal class GetCropsTask: AsyncTask<Void, Void, List<Crop>>() {
        override fun doInBackground(vararg params: Void): List<Crop> {
            return cropDao.getSavedCrops()
        }
    }

    fun getAllLiveCrops() : LiveData<List<Crop>>? {

        var retrieved : LiveData<List<Crop>>? = null
        // Execute thread
        ioThread {
            retrieved = cropDao.getLiveDataCrops()
        }

        return retrieved
    }

    fun cropDataToLoad() : Boolean {

        var numCrops = 0
        Log.d(TAG, "Checking for crops to load.")
        try {
            numCrops = CheckCropsTask().execute().get()
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }

        if (numCrops <= 0) return false

        return true

    }

    internal class CheckCropsTask: AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void): Int {
            return cropDao.getNumCrops()
        }
    }

    fun getCropByID(uid: Int) : Crop? {

        var crop: Crop? = null
        Log.d(TAG, "Retrieving crop from database w/ UID: $uid")
        try {
            crop = CropByIDTask().execute(uid).get()
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
        return crop
    }

    internal class CropByIDTask: AsyncTask<Int, Void, Crop>() {
        override fun doInBackground(vararg params: Int?): Crop? {
            return params[0]?.let { cropDao.getCropByUid(it) }
        }
    }

    fun getCropType(type: String) : List<Crop>? {

        var crops: ArrayList<Crop>? = null
        Log.d(TAG, "Retrieving Crop type '$type' from database.")
        try {
            crops = CropsByTypeTask().execute(type).get().toCollection(ArrayList())
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
        return crops
    }

    internal class CropsByTypeTask: AsyncTask<String, Void, List<Crop>>() {
        override fun doInBackground(vararg params: String?): List<Crop>? {
            return params[0]?.let { cropDao.getCropsByType(it) }
        }
    }

    fun getMemories(cropID: Int) : ArrayList<String>? {

        var retrieved : String? = null
        ioThread {
            retrieved = cropDao.getCropMemories(cropID)
        }
        if (retrieved != null) {

            val final = java.util.ArrayList<String>()
            var temp = ""
            retrieved!!.forEach {
                if (it == ',') {
                    final.add(temp)
                    temp = ""
                } else {
                    temp += it
                }
            }
            return final
        }
        return null
    }

    fun insertCrop(crop: Crop) {
        Log.d(TAG, "Inserting into database crop: $crop")
        try {
            InsertTask().execute(crop)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class InsertTask: AsyncTask<Crop, Void, Unit>() {
        override fun doInBackground(vararg params: Crop) {
            cropDao.insertOne(params[0])
        }
    }

    fun insertCrops(crops: ArrayList<Crop>) {
        Log.d(TAG, "Inserting list of crops into database.")
        try {
            InsertAllTask().execute(crops)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class InsertAllTask: AsyncTask<ArrayList<Crop>, Void, Unit>() {
        override fun doInBackground(vararg params: ArrayList<Crop>) {
            cropDao.insertAll(params[0])
        }
    }

    fun updateCrop(crop: Crop) {
        Log.d(TAG, "Updating in database crop: $crop.")
        try {
            UpdateCropTask().execute(crop)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class UpdateCropTask: AsyncTask<Crop, Void, Unit>() {
        override fun doInBackground(vararg params: Crop) {
            cropDao.updateCrop(params[0])
        }
    }

    fun updateCrops(crops: ArrayList<Crop>) {
        Log.d(TAG, "Updating list of crops in database.")
        try {
            UpdateCropsTask().execute(crops)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class UpdateCropsTask: AsyncTask<ArrayList<Crop>, Void, Unit>() {
        override fun doInBackground(vararg params: ArrayList<Crop>) {
            cropDao.updateCrops(params[0])
        }
    }

    fun updateCropName(cropId: Int, newName: String) {
        ioThread {
            cropDao.updateName(cropId, newName)
        }
    }

    fun updateCropType(cropId: Int, newType: String) {
        ioThread {
            cropDao.updateType(cropId, newType)
        }
    }

    fun updateCropGrowthTime(cropId: Int, newGrowthTime: Int) {
        ioThread {
            cropDao.updateGrowthTime(cropId, newGrowthTime)
        }
    }

    fun deleteCrop(crop: Crop) {
        Log.d(TAG, "Deleting from database crop: $crop.")
        try {
            DeleteCropTask().execute(crop)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class DeleteCropTask: AsyncTask<Crop, Void, Unit>() {
        override fun doInBackground(vararg params: Crop) {
            cropDao.deleteCrop(params[0])
        }
    }

    fun nukeCrops() {
        Log.d(TAG, "Nuking Crop database table.")
        try {
            NukeCropsTask().execute()
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class NukeCropsTask: AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void?) {
            cropDao.nukeCrops()
        }
    }

    /*****************************************************
     * End Crop Dao
     *****************************************************/

    /*****************************************************
     * Begin Note Dao
     *****************************************************/


    fun getNumNotes(): Int {
        Log.d(TAG, "Retrieving number of notes stored in database.")
        return NumNotesTask().execute().get()
    }

    internal class NumNotesTask: AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void?): Int {
            return noteDao.getNumNotes()
        }
    }

    fun getAllNotes(): List<Note>? {
        Log.d(TAG, "Retrieving all notes from database.")
        try {
            return AllNotesTask().execute().get()
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
        return null
    }

    internal class AllNotesTask: AsyncTask<Void, Void, List<Note>>() {
        override fun doInBackground(vararg params: Void?): List<Note> {
            return noteDao.getSavedNotes()
        }
    }

    fun getNoteByUid(noteID: Int) : Note? {
        var note: Note? = null
        Log.d(TAG, "Retrieving note from database w/ UID: $noteID")
        try {
            note = NoteByIDTask().execute(noteID).get()
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
        return note
    }

    internal class NoteByIDTask: AsyncTask<Int, Void, Note>() {
        override fun doInBackground(vararg params: Int?): Note? {
            return params[0]?.let { noteDao.getNoteByUid(it) }
        }
    }

    fun getNotesByCrop(name: String) : List<Note>? {
        Log.d(TAG, "Retrieving notes from database by crop: $name")
        var list: List<Note>? = null
        try {
            list = NotesByCropTask().execute(name).get()
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
        return list
    }

    internal class NotesByCropTask: AsyncTask<String, Void, List<Note>>() {
        override fun doInBackground(vararg params: String?): List<Note>? {
            return params[0]?.let { noteDao.getNotesByCrop(it) }
        }
    }

    fun getNotesByType(type: String) : List<Note>? {
        Log.d(TAG, "Retrieving notes from database by type: $type.")
        var list: List<Note>? = null
        try {
            list = NotesByTypeTask().execute(type).get()
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
        return list
    }

    internal class NotesByTypeTask: AsyncTask<String, Void, List<Note>>() {
        override fun doInBackground(vararg params: String?): List<Note>? {
            return params[0]?.let { noteDao.getNotesByType(it) }
        }
    }

    fun insertOne(newNote: Note) {
        Log.d(TAG, "Inserting into database note: $newNote.")
        try {
            InsertNoteTask().execute(newNote)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class InsertNoteTask: AsyncTask<Note, Void, Unit>() {
        override fun doInBackground(vararg params: Note) {
            noteDao.insertOne(params[0])
        }
    }

    fun insertAll(newNotes: List<Note>) {
        Log.d(TAG, "Inserting list of notes into database.")
        try {
            InsertNotesTask().execute(newNotes)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class InsertNotesTask: AsyncTask<List<Note>, Void, Unit>() {
        override fun doInBackground(vararg params: List<Note>) {
            noteDao.insertAll(params[0])
        }
    }

    fun updateNote(note: Note) {
        Log.d(TAG, "Updating in database note: $note")
        try {
            UpdateNoteTask().execute(note)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class UpdateNoteTask: AsyncTask<Note, Void, Unit>() {
        override fun doInBackground(vararg params: Note) {
            noteDao.updateNote(params[0])
        }
    }

    fun updateNotes(notes: List<Note>) {
        Log.d(TAG, "Updating list of notes in database.")
        try {
            UpdateNotesTask().execute(notes)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class UpdateNotesTask: AsyncTask<List<Note>, Void, Unit>() {
        override fun doInBackground(vararg params: List<Note>) {
            noteDao.updateNotes(params[0])
        }
    }

    fun deleteNote(delNote: Note) {
        Log.d(TAG, "Deleting from database note: $delNote")
        try {
            DeleteNoteTask().execute(delNote)
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class DeleteNoteTask: AsyncTask<Note, Void, Unit>() {
        override fun doInBackground(vararg params: Note) {
            noteDao.deleteNote(params[0])
        }
    }

    fun nukeNotes() {
        Log.d(TAG, "Nuking Note table from database.")
        try {
            NukeNotesTask().execute()
        } catch (e: ExecutionException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception caught: " + e.localizedMessage)
            e.printStackTrace()
        }
    }

    internal class NukeNotesTask: AsyncTask<Void, Void, Unit>() {
        override fun doInBackground(vararg params: Void?) {
            noteDao.nukeNotes()
        }
    }

    /*****************************************************
     * End Note Dao
     *****************************************************/

    /*****************************************************
     * Begin Memory Dao
     *****************************************************/

    fun getMemoriesForCrop(cropId: Int): List<Memory> {
        return memoryDao.getMemoriesForCrop(cropId)
    }

    fun insertMemory(memory: Memory) {
        memoryDao.insertMemory(memory)
    }

    fun deleteMemory(memory: Memory) {
        memoryDao.deleteMemory(memory)
    }

    fun deleteMemoriesByCrop(cropId: Int) {
        memoryDao.deleteMemoriesByCrop(cropId)
    }

    /*****************************************************
     * End Memory Dao
     *****************************************************/
}