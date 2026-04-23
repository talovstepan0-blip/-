package com.pluteus.data.local

import androidx.room.*
import com.pluteus.domain.model.MediaItem
import com.pluteus.domain.model.MediaType
import com.pluteus.domain.model.MediaStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items ORDER BY dateAdded DESC")
    fun getAllItems(): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY dateAdded DESC")
    fun getItemsByType(type: MediaType): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items WHERE status = :status ORDER BY dateAdded DESC")
    fun getItemsByStatus(status: MediaStatus): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items WHERE type = :type AND status = :status ORDER BY dateAdded DESC")
    fun getItemsByTypeAndStatus(type: MediaType, status: MediaStatus): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' OR creator LIKE '%' || :query || '%' ORDER BY dateAdded DESC")
    fun searchItems(query: String): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getItemById(id: Long): MediaItem?
    
    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getItemByIdFlow(id: Long): Flow<MediaItem?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MediaItem): Long
    
    @Update
    suspend fun updateItem(item: MediaItem)
    
    @Delete
    suspend fun deleteItem(item: MediaItem)
    
    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)
    
    @Query("SELECT COUNT(*) FROM media_items")
    fun getTotalCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM media_items WHERE status IN (:completedStatuses)")
    fun getCompletedCount(completedStatuses: List<MediaStatus>): Flow<Int>
}
