package com.senac.restapi.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_photos")
data class TripPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val photoUri: String,
    val addedAt: Long = System.currentTimeMillis()
)
