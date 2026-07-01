package com.senac.restapi.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.senac.restapi.database.AppDatabase
import com.senac.restapi.database.TripEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

sealed class TripOperationState {
    data object Idle : TripOperationState()
    data object Loading : TripOperationState()
    data object Success : TripOperationState()
    data class Error(val message: String) : TripOperationState()
}

sealed class LocationState {
    data object Idle : LocationState()
    data object Loading : LocationState()
    data class Success(
        val city: String,
        val latitude: Double,
        val longitude: Double,
    ) : LocationState()
    data class Error(val message: String) : LocationState()
}

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val tripDao = AppDatabase.getInstance(application).tripDao()
    private val appContext = application.applicationContext

    private val _operationState = MutableStateFlow<TripOperationState>(TripOperationState.Idle)

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState

    private val _currentTrip = MutableStateFlow<TripEntity?>(null)
    val currentTrip: StateFlow<TripEntity?> = _currentTrip

    private val _currentUserId = MutableStateFlow<Int?>(null)

    private var locationUpdateJob: Job? = null

    fun setCurrentUserId(userId: Int) {
        _currentUserId.value = userId
    }

    val userTrips: StateFlow<List<TripEntity>> = _currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        .let { userIdFlow ->
            MutableStateFlow<List<TripEntity>>(emptyList()).apply {
                viewModelScope.launch {
                    userIdFlow.collect { userId ->
                        userId?.let {
                            tripDao.getTripsByUserId(it).collect { trips ->
                                value = trips
                            }
                        }
                    }
                }
            }
        }

    fun addTrip(
        userId: Int,
        productId: Int,
        productTitle: String,
        productImage: String,
        productPrice: Double,
        tripType: String,
        startDate: Long,
        endDate: Long,
        cidade: String,
        orcamento: Double = 0.0
    ) {
        viewModelScope.launch {
            _operationState.value = TripOperationState.Loading
            try {
                val trip = TripEntity(
                    userId = userId,
                    productId = productId,
                    productTitle = productTitle,
                    productImage = productImage,
                    productPrice = productPrice,
                    tripType = tripType,
                    startDate = startDate,
                    endDate = endDate,
                    cidade = cidade,
                    orcamento = orcamento,
                    totalGastos = 0.0
                )
                tripDao.insertTrip(trip)
                _operationState.value = TripOperationState.Success
            } catch (e: Exception) {
                _operationState.value = TripOperationState.Error("Erro ao adicionar viagem: ${e.message}")
            }
        }
    }

    fun updateTrip(trip: TripEntity) {
        viewModelScope.launch {
            _operationState.value = TripOperationState.Loading
            try {
                tripDao.updateTrip(trip)
                _operationState.value = TripOperationState.Success
            } catch (e: Exception) {
                _operationState.value = TripOperationState.Error("Erro ao atualizar viagem: ${e.message}")
            }
        }
    }

    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            _operationState.value = TripOperationState.Loading
            try {
                tripDao.deleteTrip(trip)
                _operationState.value = TripOperationState.Success
            } catch (e: Exception) {
                _operationState.value = TripOperationState.Error("Erro ao excluir viagem: ${e.message}")
            }
        }
    }


    // Inicia atualizações periódicas de localização a cada 30 segundos
    fun startPeriodicLocationUpdates(userId: Int) {
        locationUpdateJob?.cancel()
        locationUpdateJob = viewModelScope.launch {
            while (isActive) {
                fetchLocationAndCurrentTrip(userId)
                delay(30_000L)
            }
        }
    }

    fun stopPeriodicLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }

    @Suppress("DEPRECATION")
    private suspend fun fetchLocationAndCurrentTrip(userId: Int) {
        _locationState.value = LocationState.Loading
        try {
            if ((ContextCompat.checkSelfPermission(
                    appContext, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(
                    appContext, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                _locationState.value = LocationState.Error("Permissão de localização negada")
                return
            }

            val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            if (location != null) {
                val geocoder = Geocoder(appContext, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val city = if (!addresses.isNullOrEmpty()) {
                    addresses[0].locality ?: addresses[0].subAdminArea ?: "Local desconhecido"
                } else {
                    "Local desconhecido"
                }

                _locationState.value = LocationState.Success(
                    city = city,
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                // Detecta viagem ativa pela data (independente da localização GPS)
                val currentDate = System.currentTimeMillis()
                _currentTrip.value = tripDao.getCurrentTripByDate(userId, currentDate)
            } else {
                _locationState.value = LocationState.Error("Localização não disponível")
                // Mesmo sem localização GPS, verifica viagem ativa pela data
                val currentDate = System.currentTimeMillis()
                _currentTrip.value = tripDao.getCurrentTripByDate(userId, currentDate)
            }
        } catch (e: Exception) {
            _locationState.value = LocationState.Error("Erro ao obter localização: ${e.message}")
        }
    }


    override fun onCleared() {
        super.onCleared()
        locationUpdateJob?.cancel()
    }
}
