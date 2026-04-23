package com.pluteus.data.repository

import com.pluteus.data.local.MediaItemDao
import com.pluteus.data.remote.NetworkModule
import com.pluteus.domain.model.*
import com.pluteus.util.ApiKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val dao: MediaItemDao) {
    
    // Local database operations
    fun getAllItems(): Flow<List<MediaItem>> = dao.getAllItems()
    
    fun getItemsByType(type: MediaType): Flow<List<MediaItem>> = dao.getItemsByType(type)
    
    fun getItemsByStatus(status: MediaStatus): Flow<List<MediaItem>> = dao.getItemsByStatus(status)
    
    fun getItemsByTypeAndStatus(type: MediaType, status: MediaStatus): Flow<List<MediaItem>> = 
        dao.getItemsByTypeAndStatus(type, status)
    
    fun searchItems(query: String): Flow<List<MediaItem>> = dao.searchItems(query)
    
    suspend fun getItemById(id: Long): MediaItem? = withContext(Dispatchers.IO) {
        dao.getItemById(id)
    }
    
    fun getItemByIdFlow(id: Long): Flow<MediaItem?> = dao.getItemByIdFlow(id)
    
    suspend fun insertItem(item: MediaItem): Long = withContext(Dispatchers.IO) {
        dao.insertItem(item)
    }
    
    suspend fun updateItem(item: MediaItem) = withContext(Dispatchers.IO) {
        dao.updateItem(item)
    }
    
    suspend fun deleteItem(item: MediaItem) = withContext(Dispatchers.IO) {
        dao.deleteItem(item)
    }
    
    fun getTotalCount(): Flow<Int> = dao.getTotalCount()
    
    // TMDB API operations
    suspend fun searchTmdb(query: String): Result<List<TmdbSearchResult>> = withContext(Dispatchers.IO) {
        try {
            if (ApiKeys.TMDB_API_KEY == "YOUR_TMDB_API_KEY_HERE") {
                Result.failure(Exception("API ключ TMDB не настроен"))
            } else {
                val response = NetworkModule.tmdbApi.searchMulti(ApiKeys.TMDB_API_KEY, query)
                val filteredResults = response.results.filter { 
                    it.mediaType == "movie" || it.mediaType == "tv" 
                }
                Result.success(filteredResults)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMovieDetails(movieId: Int): Result<TmdbDetailResponse> = withContext(Dispatchers.IO) {
        try {
            if (ApiKeys.TMDB_API_KEY == "YOUR_TMDB_API_KEY_HERE") {
                Result.failure(Exception("API ключ TMDB не настроен"))
            } else {
                val response = NetworkModule.tmdbApi.getMovieDetails(movieId, ApiKeys.TMDB_API_KEY)
                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTvDetails(tvId: Int): Result<TmdbDetailResponse> = withContext(Dispatchers.IO) {
        try {
            if (ApiKeys.TMDB_API_KEY == "YOUR_TMDB_API_KEY_HERE") {
                Result.failure(Exception("API ключ TMDB не настроен"))
            } else {
                val response = NetworkModule.tmdbApi.getTvDetails(tvId, ApiKeys.TMDB_API_KEY)
                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Google Books API operations
    suspend fun searchGoogleBooks(query: String): Result<List<VolumeInfo>> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkModule.googleBooksApi.searchBooks(query)
            val volumes = response.items?.map { it.volumeInfo } ?: emptyList()
            Result.success(volumes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
