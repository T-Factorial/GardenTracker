package com.example.GardenTracker.model

import androidx.room.*

@Entity(
    tableName = "memory",
    foreignKeys = [
        ForeignKey(
            entity = Crop::class,
            parentColumns = ["id"],
            childColumns = ["crop_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["crop_id"])]
)
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "crop_id")
    val cropId: Int,
    @ColumnInfo(name = "file_path")
    val filePath: String
)
