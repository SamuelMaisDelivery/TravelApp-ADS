package com.senac.restapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senac.restapi.api.ApiClient
import com.senac.restapi.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val api = ApiClient.productApi

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(value = false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProducts() {
        if (_products.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = api.getProducts()
                _products.value = response.products
            } catch (e: Exception) {
                _error.value = "Erro ao carregar produtos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
