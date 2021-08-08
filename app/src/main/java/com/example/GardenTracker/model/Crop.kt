package com.example.GardenTracker.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
class Crop()
    : Serializable {

    constructor(cropName: String, cropType: String, growth: Int, water: ArrayList<Int>) : this() {

        name = cropName
        type = cropType
        growthTime = growth
        waterHours = waterHoursToString(water)

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

    constructor(cy: Int, cd: Int, ch: Int, nm: String, typ: String, growth: Int,
                water: ArrayList<Int>, mems: String,
                wNeed: Boolean, harvDay: Int, readyHarv: Boolean) : this() {
        creationYear = cy
        creationDay = cd
        creationHour = ch
        name = nm
        type = typ
        growthTime = growth
        waterHours = waterHoursToString(water)
        memories = mems
        needsWater = wNeed
        harvestDay = harvDay
        readyToHarvest = readyHarv
    }

    // Entity Fields
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0

    @ColumnInfo(name = "creation_year")
    var creationYear = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.YEAR)

    @ColumnInfo(name = "creation_month")
    var creationMonth = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.MONTH)

    @ColumnInfo(name = "creation_day")
    var creationDay = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.DAY_OF_YEAR)

    @ColumnInfo(name = "creation_hour")
    var creationHour = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.HOUR_OF_DAY)

    @ColumnInfo(name = "crop_name")
    var name : String = ""

    @ColumnInfo(name = "crop_type")
    var type : String = ""

    @ColumnInfo(name = "growth_time")
    var growthTime : Int = 0

    @ColumnInfo(name = "water_hours")
    var waterHours: String = ""

    @ColumnInfo(name = "harvest_progress")
    var growthProg : Int = harvestProgress()

    @ColumnInfo(name = "crop_memories")
    var memories = ""

    @ColumnInfo(name = "current_year")
    var currentYear: Int = creationYear

    @ColumnInfo(name = "needs_water")
    var needsWater : Boolean = false

    @ColumnInfo(name = "harvest_day")
    var harvestDay : Int = 0

    @ColumnInfo(name = "ready_to_harvest")
    var readyToHarvest : Boolean = false

    fun water() {
        needsWater = false
    }

    // Assumes current date data is up-to-date
    fun updateNeedsWater(currHour: Int) {
        if (!needsWater) {
            for (hour in waterHoursFromString().reversed()) {
                if (currHour < hour) continue // Continue until
                if (currHour >= hour) { // Time for water
                    needsWater = true
                    break
                }
            }
        }
    }

    fun harvestProgress(): Int {
        val daysUntilHarvest = harvestDay - GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.DAY_OF_YEAR)
        val daysPassed = growthTime - daysUntilHarvest
        var progress = 0
        if (growthTime != 0) {
            progress = daysPassed / growthTime
        }
        return progress
    }

    private fun waterHoursToString(hours: ArrayList<Int>): String {
        var final = ""
        hours.forEach {
            final += "$it`"
        }
        final += "|"
        return final
    }

    fun setNewWaterHours(hours: ArrayList<Int>) {
        waterHours = waterHoursToString(hours)
    }

    fun waterHoursFromString(): ArrayList<Int> {
        val hours: ArrayList<Int> = ArrayList()
        var temp: String = ""
        waterHours.forEach {
            if (it != '|') {
                if (it != '`') {
                    temp = "$temp$it"
                } else {
                    hours.add(temp.toInt())
                    temp = ""
                }
            }
        }
        return hours
    }

    private fun memoriesToString(memories: List<String>): String {
        var final = ""
        memories.forEach {
            final += "`$it"
        }
        final += '|'
        return final
    }

    fun memoriesFromString(): List<String> {
        val final = ArrayList<String>()
        var temp = ""
        memories.forEach {
            if (it != '|') {
                if (it != '`') {
                    temp += it
                } else {
                    if (temp != "") {
                        final.add(temp)
                        temp = ""
                    }
                }
            }
        }
        return final
    }

    fun memoriesFromString(mems: String) : List<String> {
        val final = ArrayList<String>()
        var temp = ""
        mems.forEach {
            if (it != '|') {
                if (it != '`') {
                    temp += it
                } else {
                    if (temp != "") {
                        final.add(temp)
                        temp = ""
                    }
                }
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