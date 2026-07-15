package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarbleDao {
    @Query("SELECT * FROM marble_products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<MarbleProduct>>

    @Query("SELECT * FROM marble_products WHERE id = :id LIMIT 1")
    fun getProductById(id: Int): Flow<MarbleProduct?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: MarbleProduct): Long

    @Update
    suspend fun updateProduct(product: MarbleProduct)

    @Delete
    suspend fun deleteProduct(product: MarbleProduct)

    @Query("UPDATE marble_products SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFav: Boolean)
}
