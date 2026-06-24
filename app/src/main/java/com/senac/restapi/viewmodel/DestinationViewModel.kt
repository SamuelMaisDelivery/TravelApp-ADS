package com.senac.restapi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.senac.restapi.database.AppDatabase
import com.senac.restapi.database.DestinationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class DestinationLoadState {
    object Idle : DestinationLoadState()
    object Loading : DestinationLoadState()
    object Success : DestinationLoadState()
    data class Error(val message: String) : DestinationLoadState()
}

class DestinationViewModel(application: Application) : AndroidViewModel(application) {

    private val destinationDao = AppDatabase.getInstance(application).destinationDao()

    private val _loadState = MutableStateFlow<DestinationLoadState>(DestinationLoadState.Idle)
    val loadState: StateFlow<DestinationLoadState> = _loadState

    val destinations: StateFlow<List<DestinationEntity>> = destinationDao.getAllDestinations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Garantir que os destinos sejam populados na inicialização
        loadDestinations()
    }

    fun loadDestinations() {
        viewModelScope.launch {
            _loadState.value = DestinationLoadState.Loading
            try {
                // Verificar se existem destinos, se não, popular
                val count = destinationDao.getDestinationCount()
                if (count == 0) {
                    populateDestinations()
                }
                _loadState.value = DestinationLoadState.Success
            } catch (e: Exception) {
                _loadState.value = DestinationLoadState.Error("Erro ao carregar destinos: ${e.message}")
            }
        }
    }

    private suspend fun populateDestinations() {
        val destinations = listOf(
            DestinationEntity(
                title = "Paris, França",
                description = "Conheça a Cidade Luz! Torre Eiffel, Louvre, Champs-Élysées e muito mais. Romântica e encantadora.",
                price = 8500.00,
                imageUrl = "https://images.unsplash.com/photo-1502602898657-3e91760cbb34",
                rating = 4.8,
                cidade = "Paris",
                estado = "Île-de-France",
                tipo = "Lazer"
            ),
            DestinationEntity(
                title = "Tóquio, Japão",
                description = "Explore a fascinante capital japonesa! Cultura milenar, tecnologia de ponta e gastronomia excepcional.",
                price = 12000.00,
                imageUrl = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf",
                rating = 4.9,
                cidade = "Tóquio",
                estado = "Kantō",
                tipo = "Negócios"
            ),
            DestinationEntity(
                title = "Nova York, EUA",
                description = "A cidade que nunca dorme! Estátua da Liberdade, Times Square, Central Park e Broadway.",
                price = 9500.00,
                imageUrl = "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9",
                rating = 4.7,
                cidade = "Nova York",
                estado = "Nova York",
                tipo = "Negócios"
            ),
            DestinationEntity(
                title = "Dubai, Emirados Árabes",
                description = "Luxo e modernidade no deserto! Burj Khalifa, praias incríveis e compras espetaculares.",
                price = 11000.00,
                imageUrl = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c",
                rating = 4.6,
                cidade = "Dubai",
                estado = "Dubai",
                tipo = "Negócios"
            ),
            DestinationEntity(
                title = "Maldivas",
                description = "Paraíso tropical! Águas cristalinas, bangalôs sobre o mar e recifes de coral espetaculares.",
                price = 15000.00,
                imageUrl = "https://images.unsplash.com/photo-1514282401047-d79a71a590e8",
                rating = 5.0,
                cidade = "Malé",
                estado = "Kaafu",
                tipo = "Lazer"
            ),
            DestinationEntity(
                title = "Londres, Inglaterra",
                description = "Realeza e história! Big Ben, Palácio de Buckingham, museus e a cultura britânica.",
                price = 7500.00,
                imageUrl = "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad",
                rating = 4.7,
                cidade = "Londres",
                estado = "Inglaterra",
                tipo = "Negócios"
            ),
            DestinationEntity(
                title = "Cancún, México",
                description = "Praias caribenhas paradisíacas! Ruínas maias, vida noturna agitada e resorts all-inclusive.",
                price = 6500.00,
                imageUrl = "https://images.unsplash.com/photo-1568402102990-bc541580b59f",
                rating = 4.5,
                cidade = "Cancún",
                estado = "Quintana Roo",
                tipo = "Lazer"
            ),
            DestinationEntity(
                title = "Santorini, Grécia",
                description = "Pôr do sol inesquecível! Casinhas brancas, cúpulas azuis e vistas deslumbrantes do Mar Egeu.",
                price = 10000.00,
                imageUrl = "https://images.unsplash.com/photo-1613395877344-13d4a8e0d49e",
                rating = 4.9,
                cidade = "Santorini",
                estado = "Ilhas Cíclades",
                tipo = "Lazer"
            ),
            DestinationEntity(
                title = "Machu Picchu, Peru",
                description = "Mistério e história Inca! Trilha dos Incas, ruínas milenares e paisagens de tirar o fôlego.",
                price = 5500.00,
                imageUrl = "https://images.unsplash.com/photo-1587595431973-160d0d94add1",
                rating = 4.8,
                cidade = "Cusco",
                estado = "Cusco",
                tipo = "Lazer"
            ),
            DestinationEntity(
                title = "Bali, Indonésia",
                description = "Ilha dos Deuses! Templos sagrados, praias exóticas, arrozais verdejantes e spa relaxantes.",
                price = 8000.00,
                imageUrl = "https://images.unsplash.com/photo-1537996194471-e657df975ab4",
                rating = 4.7,
                cidade = "Denpasar",
                estado = "Bali",
                tipo = "Lazer"
            )
        )

        destinationDao.insertAllDestinations(destinations)
    }

    suspend fun getDestinationById(id: Int): DestinationEntity? {
        return destinationDao.getDestinationById(id)
    }
}
