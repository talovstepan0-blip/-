package com.pluteus.tracker.data.repository

import android.content.Context
import com.pluteus.tracker.data.local.AppDatabase
import com.pluteus.tracker.data.model.GoogleBookItem
import com.pluteus.tracker.data.model.MediaItem
import com.pluteus.tracker.data.model.MediaStatus
import com.pluteus.tracker.data.model.MediaType
import com.pluteus.tracker.data.model.TMDBResult
import com.pluteus.tracker.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaRepository(private val context: Context) {
    private val dao = AppDatabase.getDatabase(context).mediaItemDao()

    // Local database operations
    fun getAllMediaItems(): Flow<List<MediaItem>> = dao.getAllMediaItems()
    
    fun getMediaItemsByType(type: MediaType): Flow<List<MediaItem>> = dao.getMediaItemsByType(type)
    
    fun getMediaItemsByStatus(status: MediaStatus): Flow<List<MediaItem>> = dao.getMediaItemsByStatus(status)
    
    fun getMediaItemsByTypeAndStatus(type: MediaType, status: MediaStatus): Flow<List<MediaItem>> = 
        dao.getMediaItemsByTypeAndStatus(type, status)
    
    fun searchMediaItems(query: String): Flow<List<MediaItem>> = dao.searchMediaItems(query)
    
    suspend fun getMediaItemById(id: Long): MediaItem? = dao.getMediaItemById(id)
    
    fun getMediaItemByIdFlow(id: Long): Flow<MediaItem?> = dao.getMediaItemByIdFlow(id)
    
    suspend fun insertMediaItem(mediaItem: MediaItem): Long = dao.insertMediaItem(mediaItem)
    
    suspend fun updateMediaItem(mediaItem: MediaItem) = dao.updateMediaItem(mediaItem)
    
    suspend fun deleteMediaItem(mediaItem: MediaItem) = dao.deleteMediaItem(mediaItem)
    
    suspend fun deleteMediaItemById(id: Long) = dao.deleteMediaItemById(id)
    
    fun getTotalCount(): Flow<Int> = dao.getTotalCount()
    
    fun getCountByType(type: MediaType): Flow<Int> = dao.getCountByType(type)
    
    fun getCountByStatus(status: MediaStatus): Flow<Int> = dao.getCountByStatus(status)

    // TMDB API operations
    suspend fun searchTMDB(query: String, apiKey: String): Result<List<TMDBResult>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.tmdbApi.searchMulti(apiKey = apiKey, query = query)
            val filteredResults = response.results.filter { 
                it.mediaType == "movie" || it.mediaType == "tv" 
            }
            Result.success(filteredResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetails(movieId: Int, apiKey: String): Result<TMDBResult> = withContext(Dispatchers.IO) {
        try {
            val details = RetrofitInstance.tmdbApi.getMovieDetails(movieId = movieId, apiKey = apiKey)
            Result.success(details.toTMDBResult())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTVDetails(tvId: Int, apiKey: String): Result<TMDBResult> = withContext(Dispatchers.IO) {
        try {
            val details = RetrofitInstance.tmdbApi.getTVDetails(tvId = tvId, apiKey = apiKey)
            Result.success(details.toTMDBResult())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Google Books API operations
    suspend fun searchGoogleBooks(query: String): Result<List<GoogleBookItem>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitInstance.googleBooksApi.searchBooks(query = query)
            Result.success(response.items ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Image handling
    suspend fun saveImageLocally(imageUrl: String, itemId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = java.net.URL(imageUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            val inputStream = connection.getInputStream()
            
            val imageDir = File(context.filesDir, "images")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            
            val imageFile = File(imageDir, "item_$itemId.jpg")
            val outputStream = FileOutputStream(imageFile)
            
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            
            Result.success(imageFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getImageFile(itemId: Long): File? {
        val imageFile = File(context.filesDir, "images/item_$itemId.jpg")
        return if (imageFile.exists()) imageFile else null
    }

    fun deleteImageLocal(itemId: Long) {
        val imageFile = getImageFile(itemId)
        imageFile?.delete()
    }
}

// Extension function to convert TMDBDetailsResponse to TMDBResult
private fun com.pluteus.tracker.data.model.TMDBDetailsResponse.toTMDBResult(): TMDBResult {
    return TMDBResult(
        id = id,
        title = title,
        name = name,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        firstAirDate = firstAirDate,
        mediaType = if (title != null) "movie" else "tv"
    )
}
