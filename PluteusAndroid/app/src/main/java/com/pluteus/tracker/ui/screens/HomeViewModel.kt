package com.pluteus.tracker.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pluteus.tracker.data.model.MediaItem
import com.pluteus.tracker.data.model.MediaStatus
import com.pluteus.tracker.data.model.MediaType
import com.pluteus.tracker.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MediaRepository, private val context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMediaItems()
    }

    fun setFilterType(type: MediaType?) {
        _uiState.value = _uiState.value.copy(filterType = type)
        loadMediaItems()
    }

    fun setFilterStatus(status: MediaStatus?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
        loadMediaItems()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            loadMediaItems()
        } else {
            searchItems(query)
        }
    }

    fun setSortBy(sortBy: SortBy) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        loadMediaItems()
    }

    fun setSortAscending(ascending: Boolean) {
        _uiState.value = _uiState.value.copy(sortAscending = ascending)
        loadMediaItems()
    }

    fun deleteItem(item: MediaItem) {
        viewModelScope.launch {
            repository.deleteImageLocal(item.id)
            repository.deleteMediaItem(item)
        }
    }

    fun updateStatus(item: MediaItem, newStatus: MediaStatus) {
        viewModelScope.launch {
            val updatedItem = item.copy(status = newStatus)
            repository.updateMediaItem(updatedItem)
        }
    }

    private fun loadMediaItems() {
        viewModelScope.launch {
            val state = _uiState.value
            
            val flow = when {
                state.searchQuery.isNotBlank() -> repository.searchMediaItems(state.searchQuery)
                state.filterType != null && state.filterStatus != null -> 
                    repository.getMediaItemsByTypeAndStatus(state.filterType, state.filterStatus)
                state.filterType != null -> repository.getMediaItemsByType(state.filterType)
                state.filterStatus != null -> repository.getMediaItemsByStatus(state.filterStatus)
                else -> repository.getAllMediaItems()
            }
            
            flow.collect { items ->
                val sortedItems = sortItems(items, state.sortBy, state.sortAscending)
                _uiState.value = _uiState.value.copy(
                    mediaItems = sortedItems,
                    isLoading = false
                )
            }
        }
    }

    private fun searchItems(query: String) {
        viewModelScope.launch {
            repository.searchMediaItems(query).collect { items ->
                val sortedItems = sortItems(items, _uiState.value.sortBy, _uiState.value.sortAscending)
                _uiState.value = _uiState.value.copy(
                    mediaItems = sortedItems,
                    isLoading = false
                )
            }
        }
    }

    private fun sortItems(items: List<MediaItem>, sortBy: SortBy, ascending: Boolean): List<MediaItem> {
        return when (sortBy) {
            SortBy.DATE -> items.sortedBy { it.createdAt }.let { if (ascending) it.reversed() else it }
            SortBy.NAME -> items.sortedBy { it.title.lowercase() }.let { if (ascending) it else it.reversed() }
            SortBy.RATING -> {
                val (rated, unrated) = items.partition { it.rating != null }
                val sortedRated = rated.sortedBy { it.rating ?: 0f }.let { if (ascending) it else it.reversed() }
                sortedRated + unrated
            }
        }
    }

    fun getImageFile(itemId: Long): java.io.File? {
        return repository.getImageFile(itemId)
    }

    fun getTotalCount(): kotlinx.coroutines.flow.Flow<Int> = repository.getTotalCount()
    
    fun getCountByType(type: MediaType): kotlinx.coroutines.flow.Flow<Int> = repository.getCountByType(type)
    
    fun getCountByStatus(status: MediaStatus): kotlinx.coroutines.flow.Flow<Int> = repository.getCountByStatus(status)
}

enum class SortBy {
    DATE, NAME, RATING
}

data class HomeUiState(
    val mediaItems: List<MediaItem> = emptyList(),
    val filterType: MediaType? = null,
    val filterStatus: MediaStatus? = null,
    val searchQuery: String = "",
    val sortBy: SortBy = SortBy.DATE,
    val sortAscending: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)
