package com.example.GardenTracker.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

/**
 * This class contains the model for Crop data
 * A Crop should have:
 * Name, Type, Growth Time, Hours needs watered.
 * Functions to handle crop watering
 * Functions to handle growth progress/harvest progress
 * Functions to handle memory capture and saving
 */

@Entity
data class Crop(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "creation_year")
    val creationYear: Int = GregorianCalendar.getInstance(Locale("en_US@calendar=english")).get(Calendar.YEAR),

    @ColumnInfo(name = "creation_month")
    val creationMonth: Int = GregorianCalendar.getInstance(Locale("en_US@calendar=english")).get(Calendar.MONTH),

    @ColumnInfo(name = "creation_day")
    val creationDay: Int = GregorianCalendar.getInstance(Locale("en_US@calendar=english")).get(Calendar.DAY_OF_YEAR),

    @ColumnInfo(name = "creation_hour")
    val creationHour: Int = GregorianCalendar.getInstance(Locale("en_US@calendar=english")).get(Calendar.HOUR_OF_DAY),

    @ColumnInfo(name = "crop_name")
    val name: String = "",

    @ColumnInfo(name = "crop_type")
    val type: String = "",

    @ColumnInfo(name = "growth_time")
    val growthTime: Int = 0,

    @ColumnInfo(name = "water_hours")
    val waterHours: String = "",

    @ColumnInfo(name = "harvest_progress")
    val growthProg: Int = 0,

    @ColumnInfo(name = "crop_memories")
    val memories: String = "",

    @ColumnInfo(name = "needs_water")
    val needsWater: Boolean = false,

    @ColumnInfo(name = "harvest_day")
    val harvestDay: Int = 0,

    @ColumnInfo(name = "ready_to_harvest")
    val readyToHarvest: Boolean = false
) : Serializable {

    // Utility Methods
    fun water() = copy(needsWater = false)

    fun updateNeedsWater(currHour: Int): Crop {
        val hours = waterHoursFromString()
        val newNeedsWater = hours.any { it <= currHour }
        return copy(needsWater = newNeedsWater)
    }

    fun harvestProgress(): Int {
        val daysUntilHarvest = harvestDay - GregorianCalendar.getInstance(Locale("en_US@calendar=english")).get(Calendar.DAY_OF_YEAR)
        val daysPassed = growthTime - daysUntilHarvest
        return if (growthTime != 0) (daysPassed * 100 / growthTime) else 0
    }

    private fun waterHoursToString(hours: List<Int>): String =
        hours.joinToString("`", postfix = "|")

    fun waterHoursFromString(): List<Int> =
        waterHours.split('`').mapNotNull { it.toIntOrNull() }

    fun updateWaterHours(newHours: MutableList<Int>): Crop {
        val updatedWaterHours = waterHoursToString(newHours)
        return copy(waterHours = updatedWaterHours)
    }

    private fun memoriesToString(memories: List<String>): String =
        memories.joinToString("`", postfix = "|")

    fun memoriesFromString(): List<String> =
        memories.split('`').filter { it.isNotEmpty() }

    fun addMemory(memoryFile: String): Crop {
        val updatedMemories = memoriesFromString() + memoryFile
        return copy(memories = memoriesToString(updatedMemories))
    }

    fun updateName(newName: String): Crop {
        return copy(name = newName)
    }

    fun updateType(newType: String): Crop {
        return copy(type = newType)
    }

    fun updateGrowthTime(newGrowthTime: Int): Crop {
        return copy(growthTime = newGrowthTime)
    }
}
