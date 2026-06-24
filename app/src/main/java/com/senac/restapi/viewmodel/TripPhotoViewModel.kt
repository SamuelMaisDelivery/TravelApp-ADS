package com.senac.restapi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.senac.restapi.database.AppDatabase
import com.senac.restapi.database.TripPhotoEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripPhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val photoDao = AppDatabase.getInstance(application).tripPhotoDao()

    private val _tripId = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val photos: StateFlow<List<TripPhotoEntity>> = _tripId
        .flatMapLatest { tripId ->
            if (tripId != null) photoDao.getPhotosByTripId(tripId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTripId(tripId: Int) {
        _tripId.value = tripId
    }

    fun addPhoto(tripId: Int, photoUri: String) {
        viewModelScope.launch {
            photoDao.insertPhoto(TripPhotoEntity(tripId = tripId, photoUri = photoUri))
        }
    }

    fun deletePhoto(photo: TripPhotoEntity) {
        viewModelScope.launch {
            photoDao.deletePhoto(photo)
        }
    }
}
