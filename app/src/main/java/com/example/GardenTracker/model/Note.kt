package com.example.GardenTracker.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

/**
 * This class holds model data for Notes
 * Contains creation day, month and year,
 * crop name, type, and the note's content.
 */

@Entity
class Note() : Serializable {

    constructor(crop: String, type : String, content : String) : this() {
        creationDay = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.DAY_OF_MONTH)
        creationMonth = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.MONTH)
        creationYear = GregorianCalendar.getInstance(Locale("en_US@calendar=english"))
                        .get(Calendar.YEAR)
        cropName = crop
        cropType = type
        noteContent = content
    }

    constructor(day: Int, month: Int, year: Int, crop: String, type: String, content: String) : this() {
        creationDay = day
        creationMonth = month
        creationYear = year
        cropName = crop
        cropType = type
        noteContent = content
    }

    constructor(noteString: String) : this() { fromString(noteString) }

    // Entity Fields
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0

    @ColumnInfo(name = "note_day")
    var creationDay : Int = 0

    @ColumnInfo(name = "note_month")
    var creationMonth : Int = 0

    @ColumnInfo(name = "note_year")
    var creationYear : Int = 0

    @ColumnInfo(name = "crop_name")
    var cropName: String = ""

    @ColumnInfo(name = "crop_type")
    var cropType : String = ""

    @ColumnInfo(name = "note_content")
    var noteContent : String = ""

    override fun equals(other: Any?) : Boolean {

        if (other.toString() == this.toString()) {
            return true
        }

        return false
    }

    override fun toString() : String {
        var final = ""
        final += "$id`"
        final += "$creationDay`"
        final += "$creationMonth`"
        final += "$creationYear`"
        final += "$cropName`"
        final += "$cropType`"
        final += "$noteContent|"
        return final
    }

    private fun fromString(noteString : String) {
        var nid = ""
        var day = ""
        var month = ""
        var year = ""
        var name = ""
        var type = ""
        var content = ""

        var x : Int = 1

        for (ch : Char in noteString) {
            if (ch == '|') break
            if (ch != '`') when (x) {
                1 -> nid += ch
                2 -> day += ch
                3 -> month += ch
                4 -> year += ch
                5 -> name += ch
                6 -> type += ch
                7 -> content += ch
            } else {
                ++x
            }
        }

        id = nid.toInt()
        creationDay = day.toInt()
        creationMonth = month.toInt()
        creationYear = year.toInt()
        cropName = name
        cropType = type
        noteContent = content
    }
}