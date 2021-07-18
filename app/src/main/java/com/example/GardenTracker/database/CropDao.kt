package com.example.GardenTracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.GardenTracker.model.Crop

@Dao
interface CropDao {
    @Query("SELECT COUNT(crop_name) FROM crop")
    fun getNumCrops(): Int

    @Query("SELECT * FROM crop")
    fun getSavedCrops(): List<Crop>

    @Query("SELECT * FROM crop")
    fun getLiveDataCrops(): LiveData<List<Crop>>

    @Query("SELECT * FROM crop WHERE id == (:cropID)")
    fun getCropByUid(cropID: Int): Crop

    @Query("SELECT * FROM crop WHERE crop_type == (:type)")
    fun getCropsByType(type: String): List<Crop>

    @Query("SELECT crop_memories FROM crop WHERE id == (:cropID)")
    fun getCropMemories(cropID: Int): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOne(vararg newCrop: Crop)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(newCrops: List<Crop>)

    @Update
    fun updateCrop(crop: Crop)

    @Update // ???
    fun updateCrops(crops: List<Crop>)

    @Delete
    fun deleteCrop(delCrop: Crop)

    @Query("DELETE FROM crop")
    fun nukeCrops()
}