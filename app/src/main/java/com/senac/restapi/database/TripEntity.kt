package com.senac.restapi.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val productTitle: String,
    val productImage: String,
    val productPrice: Double,
    val tripType: String, // "Lazer", "Negócios", "Outros"
    val startDate: Long, // timestamp
    val endDate: Long, // timestamp
    val cidade: String = "", // Cidade da viagem
    val orcamento: Double = 0.0, // Orçamento planejado para a viagem
    val totalGastos: Double = 0.0, // Total de gastos realizados na viagem
)
