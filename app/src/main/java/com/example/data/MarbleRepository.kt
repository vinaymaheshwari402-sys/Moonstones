package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MarbleRepository(private val marbleDao: MarbleDao) {

    val allProducts: Flow<List<MarbleProduct>> = marbleDao.getAllProducts()

    fun getProductById(id: Int): Flow<MarbleProduct?> {
        return marbleDao.getProductById(id)
    }

    suspend fun insertProduct(product: MarbleProduct): Long {
        return marbleDao.insertProduct(product)
    }

    suspend fun updateProduct(product: MarbleProduct) {
        marbleDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: MarbleProduct) {
        marbleDao.deleteProduct(product)
    }

    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {
        marbleDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun prepopulateIfEmpty() {
        val currentList = allProducts.first()
        if (currentList.isEmpty()) {
            val sampleProducts = listOf(
                MarbleProduct(
                    name = "Statuario Extra Premium",
                    collectionName = "Imperial Statuario",
                    category = "Italian Marble",
                    marbleType = "Statuario",
                    finish = "Polished",
                    thickness = "18mm",
                    color = "White",
                    origin = "Italy",
                    skuNumber = "MS-ITA-STAT-001",
                    batchNumber = "B-2026-09",
                    lotNumber = "LOT-992A",
                    storeName = "Kishangarh Showroom",
                    totalSlabs = 5,
                    availableSlabs = 3,
                    reservedSlabs = 1,
                    soldSlabs = 1,
                    slabsJson = "122x74;120x72;124x75;118x70;121x73",
                    blockQuantity = 1,
                    containerNumber = "CON-ITA-0092",
                    warehouseLocation = "Main Block A",
                    rackNumber = "Rack-04",
                    coverImageUrl = "preset_statuario",
                    galleryImagesJson = "preset_statuario_1,preset_statuario_2",
                    bookmatchImageUrl = "preset_statuario_bookmatch",
                    videoUrl = "",
                    isFavorite = true,
                    price = 850.0
                ),
                MarbleProduct(
                    name = "Nero Portoro Gold Royale",
                    collectionName = "Royale Obsidian",
                    category = "Italian Marble",
                    marbleType = "Portoro Black",
                    finish = "Polished",
                    thickness = "20mm",
                    color = "Black",
                    origin = "Italy",
                    skuNumber = "MS-ITA-PORT-004",
                    batchNumber = "B-2026-11",
                    lotNumber = "LOT-105B",
                    storeName = "Main Depot Kishangarh",
                    totalSlabs = 4,
                    availableSlabs = 2,
                    reservedSlabs = 1,
                    soldSlabs = 1,
                    slabsJson = "110x68;112x70;108x66;115x71",
                    blockQuantity = 2,
                    containerNumber = "CON-ITA-0421",
                    warehouseLocation = "Exotics Wing B",
                    rackNumber = "Rack-12",
                    coverImageUrl = "preset_portoro",
                    galleryImagesJson = "preset_portoro_1,preset_portoro_2",
                    bookmatchImageUrl = "preset_portoro_bookmatch",
                    videoUrl = "",
                    isFavorite = false,
                    price = 1250.0
                ),
                MarbleProduct(
                    name = "Michelangelo Golden Aura",
                    collectionName = "Golden Michelangelo",
                    category = "Premium Onyx",
                    marbleType = "Michel Angelo",
                    finish = "Honed",
                    thickness = "18mm",
                    color = "Golden",
                    origin = "India",
                    skuNumber = "MS-IND-MICH-012",
                    batchNumber = "B-2026-04",
                    lotNumber = "LOT-882C",
                    storeName = "Kishangarh Showroom",
                    totalSlabs = 3,
                    availableSlabs = 2,
                    reservedSlabs = 1,
                    soldSlabs = 0,
                    slabsJson = "115x72;118x74;114x70",
                    blockQuantity = 0,
                    containerNumber = "CON-IND-9923",
                    warehouseLocation = "Onyx Gallery C",
                    rackNumber = "Rack-01",
                    coverImageUrl = "preset_michelangelo",
                    galleryImagesJson = "preset_michelangelo_1",
                    bookmatchImageUrl = "preset_michelangelo_bookmatch",
                    videoUrl = "",
                    isFavorite = true,
                    price = 950.0
                ),
                MarbleProduct(
                    name = "Taj Mahal Exotic Quartzite",
                    collectionName = "Exotic Quartzites",
                    category = "Quartzite",
                    marbleType = "Taj Mahal",
                    finish = "Leathered",
                    thickness = "20mm",
                    color = "Beige",
                    origin = "Brazil",
                    skuNumber = "MS-BRZ-TAJ-008",
                    batchNumber = "B-2026-15",
                    lotNumber = "LOT-771F",
                    storeName = "Secondary Yard",
                    totalSlabs = 2,
                    availableSlabs = 2,
                    reservedSlabs = 0,
                    soldSlabs = 0,
                    slabsJson = "130x78;128x76",
                    blockQuantity = 3,
                    containerNumber = "CON-BRZ-4101",
                    warehouseLocation = "Quartzite Yard",
                    rackNumber = "Rack-22",
                    coverImageUrl = "preset_tajmahal",
                    galleryImagesJson = "preset_tajmahal_1,preset_tajmahal_2",
                    bookmatchImageUrl = "",
                    videoUrl = "",
                    isFavorite = false,
                    price = 1450.0
                )
            )
            for (product in sampleProducts) {
                marbleDao.insertProduct(product)
            }
        }
    }
}
