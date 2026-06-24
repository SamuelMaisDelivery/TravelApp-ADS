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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

sealed class TripOperationState {
    object Idle : TripOperationState()
    object Loading : TripOperationState()
    object Success : TripOperationState()
    data class Error(val message: String) : TripOperationState()
}

sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(val city: String) : LocationState()
    data class Error(val message: String) : LocationState()
}

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val tripDao = AppDatabase.getInstance(application).tripDao()

    private val _operationState = MutableStateFlow<TripOperationState>(TripOperationState.Idle)
    val operationState: StateFlow<TripOperationState> = _operationState

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState

    private val _currentTrip = MutableStateFlow<TripEntity?>(null)
    val currentTrip: StateFlow<TripEntity?> = _currentTrip

    private val _currentUserId = MutableStateFlow<Int?>(null)

    fun setCurrentUserId(userId: Int) {
        _currentUserId.value = userId
    }

    val userTrips: StateFlow<List<TripEntity>> = _currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        .let { userIdFlow ->
            MutableStateFlow<List<TripEntity>>(emptyList()).apply {
                viewModelScope.launch {
                    userIdFlow.collect { userId ->
                        if (userId != null) {
                            tripDao.getTripsByUserId(userId).collect { trips ->
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

    fun resetOperationState() {
        _operationState.value = TripOperationState.Idle
    }

    @Suppress("DEPRECATION")
    fun getCurrentLocation(context: Context, userId: Int) {
        viewModelScope.launch {
            _locationState.value = LocationState.Loading
            try {
                // Verifica permissões
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    _locationState.value = LocationState.Error("Permissão de localização negada")
                    return@launch
                }

                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "Cidade desconhecida"
                        _locationState.value = LocationState.Success(city)

                        // Busca viagem atual baseada na cidade e userId
                        val currentDate = System.currentTimeMillis()
                        val trip = tripDao.getCurrentTripByCity(userId, city, currentDate)
                        _currentTrip.value = trip
                    } else {
                        _locationState.value = LocationState.Error("Não foi possível obter a cidade")
                    }
                } else {
                    _locationState.value = LocationState.Error("Localização não disponível")
                }
            } catch (e: Exception) {
                _locationState.value = LocationState.Error("Erro ao obter localização: ${e.message}")
            }
        }
    }

    fun searchCurrentTripByCity(userId: Int, city: String) {
        viewModelScope.launch {
            try {
                val currentDate = System.currentTimeMillis()
                val trip = tripDao.getCurrentTripByCity(userId, city, currentDate)
                _currentTrip.value = trip
            } catch (e: Exception) {
                _locationState.value = LocationState.Error("Erro ao buscar viagem: ${e.message}")
            }
        }
    }
}
