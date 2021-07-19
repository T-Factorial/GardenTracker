package com.example.GardenTracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
class Crop()
    : Serializable {

    var cropName : String = "N/A"
    var cropType : String = "N/A"
    var growthTime : Int = 0
    var waterFreq : Int = 0

    constructor(name: String, type: String, growth: Int, water: Int) : this() {

        cropName = name
        cropType = type
        growthTime = growth
        waterFreq = water

        // Calculate growth time
        harvestDay = 0
        var daysUntilNewYear = 0
        daysUntilNewYear = if (creationYear % 4 == 0) {
            366 - creationDay
        } else {
            365 - creationDay
        }
        harvestDay = if (growthTime > daysUntilNewYear) {
            growthTime - daysUntilNewYear
        } else {
            creationDay + growthTime
        }

        readyToHarvest = false
    }

    constructor(cy: Int, cd: Int, ch: Int, nm: String, typ: String,
                mems: String, currDay: Int, currHour: Int,
                wLast: Int, wNeed: Boolean, dehyd: Boolean,
                harvDay: Int, hrBtwWater: Int, nextWater: Int,
                readyHarv: Boolean) : this() {
        creationYear = cy
        creationDay = cd
        creationHour = ch
        name = nm
        type = typ
        memories = mems
        currentDay = currDay
        currentHour = currHour
        lastWater = wLast
        needsWater = wNeed
        dehydrated = dehyd
        harvestDay = harvDay
        hoursBetweenWater = hrBtwWater
        nextWaterHour = nextWater
        readyToHarvest = readyHarv
    }

    // Entity Fields
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0

    @ColumnInfo(name = "creation_year")
    var creationYear = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.YEAR)

    @ColumnInfo(name = "creation_day")
    var creationDay = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.DAY_OF_YEAR)

    @ColumnInfo(name = "creation_hour")
    var creationHour = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.HOUR_OF_DAY)

    @ColumnInfo(name = "crop_name")
    var name : String = cropName

    @ColumnInfo(name = "crop_type")
    var type : String = cropType

    @ColumnInfo(name = "harvest_progress")
    var growthProg : Int = harvestProgress()

    @ColumnInfo(name = "water_progress")
    var waterProg : Int = waterProgress()

    @ColumnInfo(name = "crop_memories")
    var memories = ""

    @ColumnInfo(name = "current_day")
    var currentDay  : Int = creationDay

    @ColumnInfo(name = "current_hour")
    var currentHour : Int = creationHour

    @ColumnInfo(name = "last_water")
    var lastWater : Int = currentHour

    @ColumnInfo(name = "needs_water")
    var needsWater : Boolean = true

    @ColumnInfo(name = "dehydrated")
    var dehydrated : Boolean = false

    @ColumnInfo(name = "harvest_day")
    var harvestDay : Int = 0

    @ColumnInfo(name = "water_frequency_hours")
    var hoursBetweenWater : Int = 0

    @ColumnInfo(name = "next_water")
    var nextWaterHour : Int = 0

    @ColumnInfo(name = "ready_to_harvest")
    var readyToHarvest : Boolean = false


    fun setReadyToHarvest() { readyToHarvest = true }
   // fun readyToHarvest() : Boolean { return readyToHarvest }
    fun water() {
        needsWater = false
        lastWater = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                    .get(Calendar.HOUR_OF_DAY)
    }
    fun watered() : Boolean { return !needsWater }
    fun setDehydrated() {
        dehydrated = true
    }

    fun setCurrentD(day: Int) { currentDay = day }
    fun setCurrentT(hour: Int) { currentHour = hour }

    fun harvestProgress(): Int {
        val daysUntilHarvest = harvestDay - currentDay
        val daysPassed = growthTime - daysUntilHarvest
        var progress = 0
        if (growthTime != 0) {
            progress = daysPassed / growthTime
        }
        return progress
    }
    fun waterProgress() : Int {
        val hoursUntilWater = nextWaterHour - currentHour
        val hoursPassed = hoursBetweenWater - hoursUntilWater
        var progress = 0
        if (hoursBetweenWater != 0) {
            progress = hoursPassed / hoursBetweenWater
        }
        return progress
    }

    private fun memoriesToString(memories: List<String>) : String {
        var final = ""
        memories.forEach {
            final += "$it,"
        }
        final.dropLast(1)
        return final
    }

    fun memoriesFromString() : List<String> {
        val final = ArrayList<String>()
        var temp = ""
        memories.forEach {
            if (it == ',') {
                final.add(temp)
                temp = ""
            } else {
                temp += it
            }
        }
        return final
    }

    fun memoriesFromString(mems: String) : List<String> {
        val final = ArrayList<String>()
        var temp = ""
        mems.forEach {
            if (it == ',') {
                final.add(temp)
                temp = ""
            } else {
                temp += it
            }
        }
        return final
    }

    fun addMemory(memoryFile: String) {
        val memoryList = memoriesFromString().toCollection(ArrayList())
        memoryList.add(memoryFile)
        memories = memoriesToString(memoryList)
    }

}