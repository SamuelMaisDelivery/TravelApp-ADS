package com.senac.restapi.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY startDate DESC")
    fun getTripsByUserId(userId: Int): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTripById(tripId: Int): TripEntity?

    @Query("SELECT * FROM trips WHERE userId = :userId AND LOWER(cidade) = LOWER(:cidade) AND :currentDate BETWEEN startDate AND endDate LIMIT 1")
    suspend fun getCurrentTripByCity(userId: Int, cidade: String, currentDate: Long): TripEntity?

    @Query("SELECT * FROM trips WHERE userId = :userId AND :currentDate BETWEEN startDate AND endDate ORDER BY startDate DESC LIMIT 1")
    suspend fun getCurrentTripByDate(userId: Int, currentDate: Long): TripEntity?
}
