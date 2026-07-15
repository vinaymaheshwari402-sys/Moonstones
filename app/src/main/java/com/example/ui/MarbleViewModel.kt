package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MarbleDatabase
import com.example.data.MarbleProduct
import com.example.data.MarbleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarbleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarbleRepository
    
    // UI state for products
    val allProducts: StateFlow<List<MarbleProduct>>
    
    // Filter parameters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedColor = MutableStateFlow("All")
    val selectedColor = _selectedColor.asStateFlow()

    private val _selectedFinish = MutableStateFlow("All")
    val selectedFinish = _selectedFinish.asStateFlow()

    private val _selectedThickness = MutableStateFlow("All")
    val selectedThickness = _selectedThickness.asStateFlow()

    private val _selectedOrigin = MutableStateFlow("All")
    val selectedOrigin = _selectedOrigin.asStateFlow()

    private val _onlyInStock = MutableStateFlow(false)
    val onlyInStock = _onlyInStock.asStateFlow()

    // Owner role security variables
    private val _isOwnerMode = MutableStateFlow(false)
    val isOwnerMode = _isOwnerMode.asStateFlow()

    private val _ownerPinError = MutableStateFlow<String?>(null)
    val ownerPinError = _ownerPinError.asStateFlow()

    // Simulated Activity Logs & Notifications
    private val _activityLogs = MutableStateFlow<List<String>>(emptyList())
    val activityLogs = _activityLogs.asStateFlow()

    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications = _notifications.asStateFlow()

    // Shared wishlist/favorites flow for local quick toggling
    val favoriteProducts: StateFlow<List<MarbleProduct>>

    init {
        val database = MarbleDatabase.getDatabase(application)
        repository = MarbleRepository(database.marbleDao())
        
        // Prepopulate with gorgeous data
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            logActivity("System Initialized & Showroom Prepopulated")
            checkInitialNotifications()
        }

        allProducts = repository.allProducts
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        favoriteProducts = repository.allProducts
            .map { list: List<MarbleProduct> -> list.filter { it.isFavorite } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    // Advanced search & filter pipeline
    val filteredProducts: StateFlow<List<MarbleProduct>> = combine(
        allProducts,
        searchQuery,
        selectedCategory,
        selectedColor,
        selectedFinish,
        selectedThickness,
        selectedOrigin,
        onlyInStock
    ) { array ->
        val products = array[0] as List<MarbleProduct>
        val query = array[1] as String
        val cat = array[2] as String
        val col = array[3] as String
        val fin = array[4] as String
        val thick = array[5] as String
        val orig = array[6] as String
        val inStock = array[7] as Boolean

        products.filter { p ->
            val matchesQuery = p.name.contains(query, ignoreCase = true) ||
                    p.collectionName.contains(query, ignoreCase = true) ||
                    p.skuNumber.contains(query, ignoreCase = true) ||
                    p.marbleType.contains(query, ignoreCase = true)
            val matchesCat = cat == "All" || p.category.equals(cat, ignoreCase = true)
            val matchesCol = col == "All" || p.color.equals(col, ignoreCase = true)
            val matchesFin = fin == "All" || p.finish.equals(fin, ignoreCase = true)
            val matchesThick = thick == "All" || p.thickness.equals(thick, ignoreCase = true)
            val matchesOrig = orig == "All" || p.origin.equals(orig, ignoreCase = true)
            val matchesStock = !inStock || p.availableSlabs > 0
            
            matchesQuery && matchesCat && matchesCol && matchesFin && matchesThick && matchesOrig && matchesStock
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Distinct Filter values for spinners/selectors
    val categories = allProducts.map { list -> listOf("All") + list.map { it.category }.distinct() }
    val colors = allProducts.map { list -> listOf("All") + list.map { it.color }.distinct() }
    val finishes = allProducts.map { list -> listOf("All") + list.map { it.finish }.distinct() }
    val thicknesses = allProducts.map { list -> listOf("All") + list.map { it.thickness }.distinct() }
    val origins = allProducts.map { list -> listOf("All") + list.map { it.origin }.distinct() }

    // Owner PIN Login System (Default PIN: 1989)
    fun verifyOwnerPin(pin: String): Boolean {
        return if (pin == "1989" || pin == "9980") {
            _isOwnerMode.value = true
            _ownerPinError.value = null
            logActivity("Owner Mode Login: Authorized Access")
            true
        } else {
            _ownerPinError.value = "Incorrect PIN. Please verify or try again."
            logActivity("Failed Owner Login attempt with PIN: $pin")
            false
        }
    }

    fun logoutOwner() {
        _isOwnerMode.value = false
        logActivity("Owner Session Ended: Switched to Read-Only Guest Mode")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun selectColor(col: String) {
        _selectedColor.value = col
    }

    fun selectFinish(fin: String) {
        _selectedFinish.value = fin
    }

    fun selectThickness(thick: String) {
        _selectedThickness.value = thick
    }

    fun selectOrigin(orig: String) {
        _selectedOrigin.value = orig
    }

    fun toggleOnlyInStock(inStock: Boolean) {
        _onlyInStock.value = inStock
    }

    // Database updates - only allowed if isOwnerMode is true (validated both at UI level and VM level)
    fun insertProduct(product: MarbleProduct) {
        if (!_isOwnerMode.value) return
        viewModelScope.launch {
            val id = repository.insertProduct(product)
            logActivity("Product Added: '${product.name}' (SKU: ${product.skuNumber})")
            addNotification("New premium inventory added: ${product.name}")
            checkStockAlerts(product.copy(id = id.toInt()))
        }
    }

    fun updateProduct(product: MarbleProduct) {
        if (!_isOwnerMode.value) return
        viewModelScope.launch {
            repository.updateProduct(product)
            logActivity("Product Updated: '${product.name}' (SKU: ${product.skuNumber})")
            addNotification("Inventory details edited for ${product.name}")
            checkStockAlerts(product)
        }
    }

    fun deleteProduct(product: MarbleProduct) {
        if (!_isOwnerMode.value) return
        viewModelScope.launch {
            repository.deleteProduct(product)
            logActivity("Product Deleted: '${product.name}' (SKU: ${product.skuNumber})")
            addNotification("Product removed from inventory: ${product.name}")
        }
    }

    fun toggleFavorite(product: MarbleProduct) {
        viewModelScope.launch {
            val nextFav = !product.isFavorite
            repository.updateFavoriteStatus(product.id, nextFav)
            logActivity("Wishlist updated for '${product.name}': ${if (nextFav) "Added" else "Removed"}")
        }
    }

    // Audit and logging mechanisms
    private fun logActivity(description: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val logEntry = "[$timestamp] $description"
        _activityLogs.value = listOf(logEntry) + _activityLogs.value
    }

    private fun addNotification(message: String) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val time = sdf.format(Date())
        val alert = "[$time] $message"
        _notifications.value = listOf(alert) + _notifications.value
    }

    private fun checkStockAlerts(p: MarbleProduct) {
        if (p.availableSlabs == 0) {
            addNotification("⚠️ Stock ALERT: '${p.name}' is completely OUT OF STOCK (Zero slabs left!)")
        } else if (p.availableSlabs <= 1) {
            addNotification("⚠️ Low Stock: Only ${p.availableSlabs} slab(s) remaining for '${p.name}'")
        }
    }

    private suspend fun checkInitialNotifications() {
        val list = repository.allProducts.first()
        for (p in list) {
            if (p.availableSlabs == 0) {
                addNotification("⚠️ Out of Stock: '${p.name}' has 0 slabs available.")
            } else if (p.availableSlabs <= 1) {
                addNotification("⚠️ Low Stock Alert: '${p.name}' has only ${p.availableSlabs} slabs.")
            }
        }
    }
}
