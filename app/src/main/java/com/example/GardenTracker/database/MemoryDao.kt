package com.example.GardenTracker.database

import androidx.room.*
import com.example.GardenTracker.model.Memory

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory WHERE crop_id = :cropId")
    fun getMemoriesForCrop(cropId: Int): List<Memory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMemory(memory: Memory)

    @Delete
    fun deleteMemory(memory: Memory)

    @Query("DELETE FROM memory WHERE crop_id = :cropId")
    fun deleteMemoriesByCrop(cropId: Int)
}