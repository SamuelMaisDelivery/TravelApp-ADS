package com.senac.restapi.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destinations")
data class DestinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val rating: Double,
    val cidade: String,
    val estado: String,
    val tipo: String, // "Lazer" ou "Negócios"
)
