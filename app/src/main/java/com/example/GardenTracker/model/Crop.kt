package com.example.GardenTracker.model

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
                water: ArrayList<Int>, mems: String, currDay: Int, currHour: Int,
                wNeed: Boolean, harvDay: Int, readyHarv: Boolean) : this() {
        creationYear = cy
        creationDay = cd
        creationHour = ch
        name = nm
        type = typ
        growthTime = growth
        waterHours = waterHoursToString(water)
        memories = mems
        currentDay = currDay
        currentHour = currHour
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
                        .get(Calendar.DAY_OF_MONTH)

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

    @ColumnInfo(name = "current_month")
    var currentMonth: Int = creationMonth

    @ColumnInfo(name = "current_day")
    var currentDay  : Int = creationDay

    @ColumnInfo(name = "current_hour")
    var currentHour : Int = creationHour

    @ColumnInfo(name = "needs_water")
    var needsWater : Boolean = false

    @ColumnInfo(name = "harvest_day")
    var harvestDay : Int = 0

    @ColumnInfo(name = "ready_to_harvest")
    var readyToHarvest : Boolean = false


    fun setReadyToHarvest() { readyToHarvest = true }

    // Assumes current date data is up-to-date
    fun checkReadyToHarvest(): Boolean {
        if (harvestDay <= currentDay) return true
        return false
    }
    fun water() {
        needsWater = false
    }

    // Assumes current date data is up-to-date
    fun updateNeedsWater() {
        for (hour in waterHoursFromString().reversed()) {
            if (currentHour < hour) continue // Continue until
            if (currentHour >= hour) { // Time for water
                needsWater = true
                break
            }
        }
    }

    // Update the current year
    fun updateCurrentY() {
        currentYear = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.YEAR)
    }
    // Update the current month
    fun updateCurrentM() {
        currentMonth = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.MONTH)
    }
    // Update the current day
    fun updateCurrentD() {
        currentDay = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.DAY_OF_MONTH)
    }
    // Update the current hour
    fun updateCurrentH() {
        currentHour = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
            .get(Calendar.HOUR_OF_DAY)
    }

    // Update current date data
    fun updateCropDate() {
        updateCurrentY()
        updateCurrentM()
        updateCurrentD()
        updateCurrentH()
    }

    fun harvestProgress(): Int {
        val daysUntilHarvest = harvestDay - currentDay
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

    fun waterHoursFromString(): ArrayList<Int> {
        val hours: ArrayList<Int> = ArrayList()
        waterHours.forEach {
            if (it != '|') {
                if (it != '`') {
                    hours.add(it.digitToInt())
                }
            }
        }
        return hours
    }

    private fun memoriesToString(memories: List<String>): String {
        var final = ""
        memories.forEach {
            final += "$it,"
        }
        final.dropLast(1)
        return final
    }

    fun memoriesFromString(): List<String> {
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