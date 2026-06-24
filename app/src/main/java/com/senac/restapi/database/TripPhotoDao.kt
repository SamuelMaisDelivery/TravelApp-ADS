package com.senac.restapi.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: TripPhotoEntity): Long

    @Delete
    suspend fun deletePhoto(photo: TripPhotoEntity)

    @Query("SELECT * FROM trip_photos WHERE tripId = :tripId ORDER BY addedAt DESC")
    fun getPhotosByTripId(tripId: Int): Flow<List<TripPhotoEntity>>
}
