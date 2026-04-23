package com.pluteus.tracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pluteus.tracker.data.model.MediaItem
import com.pluteus.tracker.data.model.MediaStatus
import com.pluteus.tracker.data.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items ORDER BY createdAt DESC")
    fun getAllMediaItems(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY createdAt DESC")
    fun getMediaItemsByType(type: MediaType): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE status = :status ORDER BY createdAt DESC")
    fun getMediaItemsByStatus(status: MediaStatus): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE type = :type AND status = :status ORDER BY createdAt DESC")
    fun getMediaItemsByTypeAndStatus(type: MediaType, status: MediaStatus): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchMediaItems(query: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getMediaItemById(id: Long): MediaItem?

    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getMediaItemByIdFlow(id: Long): Flow<MediaItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaItem(mediaItem: MediaItem): Long

    @Update
    suspend fun updateMediaItem(mediaItem: MediaItem)

    @Delete
    suspend fun deleteMediaItem(mediaItem: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaItemById(id: Long)

    @Query("SELECT COUNT(*) FROM media_items")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_items WHERE type = :type")
    fun getCountByType(type: MediaType): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_items WHERE status = :status")
    fun getCountByStatus(status: MediaStatus): Flow<Int>
}
