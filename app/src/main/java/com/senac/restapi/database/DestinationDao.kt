package com.senac.restapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: DestinationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDestinations(destinations: List<DestinationEntity>)

    @Query("SELECT * FROM destinations ORDER BY title ASC")
    fun getAllDestinations(): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM destinations WHERE id = :destinationId LIMIT 1")
    suspend fun getDestinationById(destinationId: Int): DestinationEntity?

    @Query("SELECT COUNT(*) FROM destinations")
    suspend fun getDestinationCount(): Int
}
