package com.senac.restapi.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.senac.restapi.database.AppDatabase
import com.senac.restapi.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ItineraryTripInfo(
    val title: String,
    val city: String,
    val startDate: Long,
    val endDate: Long,
    val tripType: String,
)

sealed class ItineraryState {
    data object Idle : ItineraryState()
    data object Loading : ItineraryState()
    data class Success(
        val tripInfo: ItineraryTripInfo,
        val itinerary: String,
    ) : ItineraryState()
    data class Error(val message: String) : ItineraryState()
}

class ItineraryViewModel(application: Application) : AndroidViewModel(application) {

    private val tripDao = AppDatabase.getInstance(application).tripDao()
    private val repository = GeminiRepository()

    private val _state = MutableStateFlow<ItineraryState>(ItineraryState.Idle)
    val state: StateFlow<ItineraryState> = _state.asStateFlow()

    fun generateItinerary(tripId: Int) {
        if (_state.value is ItineraryState.Loading) return
        Log.d(TAG, "generateItinerary() → tripId=$tripId")
        viewModelScope.launch {
            _state.value = ItineraryState.Loading
            try {
                val trip = tripDao.getTripById(tripId)
                Log.d(TAG, "Trip from DB: ${trip?.productTitle ?: "NOT FOUND"}")
                if (trip == null) throw Exception("Viagem não encontrada.")
                repository.generateItinerary(trip).fold(
                    onSuccess = { itinerary ->
                        Log.d(TAG, "Success — itinerary length: ${itinerary.length}")
                        _state.value = ItineraryState.Success(
                            tripInfo = ItineraryTripInfo(
                                title = trip.productTitle,
                                city = trip.cidade,
                                startDate = trip.startDate,
                                endDate = trip.endDate,
                                tripType = trip.tripType,
                            ),
                            itinerary = itinerary
                        )
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Repository failure: ${error.message}", error)
                        _state.value = ItineraryState.Error(error.message ?: "Erro desconhecido ao gerar roteiro.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "ViewModel catch: ${e.message}", e)
                _state.value = ItineraryState.Error(e.message ?: "Erro ao gerar roteiro.")
            }
        }
    }

    companion object {
        private const val TAG = "ItineraryViewModel"
    }

    fun retry(tripId: Int) = generateItinerary(tripId)

    fun reset() {
        _state.value = ItineraryState.Idle
    }
}
