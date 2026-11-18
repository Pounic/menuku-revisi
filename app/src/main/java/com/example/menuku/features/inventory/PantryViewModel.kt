package com.example.menuku.features.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.menuku.data.models.StockItem
import com.example.menuku.data.models.StorageLocation
import com.example.menuku.data.repository.MenuKuRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class PantryViewModel(private val repository: MenuKuRepository) : ViewModel() {
    // Refresh date-dependent labels while the app remains open overnight.
    private val clock = flow { while (true) { emit(System.currentTimeMillis()); delay(60_000) } }
    val stockItems = combine(repository.stockItems, clock) { items, _ ->
        items.map { it.copy(remainingDays = it.daysUntilExpiry) }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.stockItems.value)
    val storageLocations = repository.storageLocations
    val recipes = repository.recipes
    fun addStockItem(item: StockItem) { validate(item); repository.addStockItem(item) }
    fun updateStockItem(item: StockItem) { validate(item); repository.updateStockItem(item) }
    fun deleteStockItem(id: String) = repository.deleteStockItem(id)
    fun updateStockQuantity(id: String, quantity: Double) = repository.updateStockQuantity(id, quantity)
    fun moveStockToLocation(id: String, location: StorageLocation) = repository.moveStockToLocation(id, location)
    fun addStorageLocation(label: String, description: String) = repository.addStorageLocation(label, description)
    private fun validate(item: StockItem) {
        require(item.name.isNotBlank() && item.unit.isNotBlank())
        require(item.quantity.isFinite() && item.quantity > 0)
    }
    class Factory(private val repository: MenuKuRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(PantryViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return PantryViewModel(repository) as T
        }
    }
}
