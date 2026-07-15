package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marble_products")
data class MarbleProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val collectionName: String,
    val category: String, // e.g. Italian Marble, Granite, Quartzite, Onyx, Sandstone
    val marbleType: String, // e.g. Statuario, Calacatta, Portoro, Michel Angelo
    val finish: String, // e.g. Polished, Honed, Leathered, Bookmatch
    val thickness: String, // e.g. 18mm, 20mm, 30mm
    val color: String, // e.g. White, Black, Golden, Grey, Beige
    val origin: String, // e.g. Italy, India, Brazil, Turkey
    val skuNumber: String,
    val batchNumber: String,
    val lotNumber: String,
    
    // Inventory details
    val storeName: String,
    val totalSlabs: Int,
    val availableSlabs: Int,
    val reservedSlabs: Int,
    val soldSlabs: Int,
    val slabsJson: String = "", // Formatted as "120x72;118x70;122x74"
    val blockQuantity: Int = 0,
    val containerNumber: String = "",
    val warehouseLocation: String = "",
    val rackNumber: String = "",
    
    // Images & media
    val coverImageUrl: String = "",
    val galleryImagesJson: String = "", // Comma-separated image URLs or resource names
    val bookmatchImageUrl: String = "",
    val videoUrl: String = "",
    
    // Extra options
    val isFavorite: Boolean = false,
    val price: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Parse the slabsJson string into a list of SlabDimensions
    fun getSlabDimensionsList(): List<SlabDimension> {
        if (slabsJson.isBlank()) return emptyList()
        return try {
            slabsJson.split(";")
                .filter { it.isNotBlank() }
                .map {
                    val parts = it.split("x")
                    SlabDimension(parts[0].toDouble(), parts[1].toDouble())
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Parse gallery images
    fun getGalleryImagesList(): List<String> {
        if (galleryImagesJson.isBlank()) return emptyList()
        return galleryImagesJson.split(",").filter { it.isNotBlank() }
    }

    // Area calculations
    fun calculateTotalAreaSqFt(): Double {
        return getSlabDimensionsList().sumOf { it.areaSqFt }
    }

    fun calculateTotalAreaSqM(): Double {
        return getSlabDimensionsList().sumOf { it.areaSqM }
    }

    fun calculateAverageSizeString(): String {
        val list = getSlabDimensionsList()
        if (list.isEmpty()) return "N/A"
        val avgL = list.map { it.length }.average()
        val avgW = list.map { it.width }.average()
        return String.format("%.1f\" x %.1f\"", avgL, avgW)
    }
}
