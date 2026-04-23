package com.pluteus.tracker.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pluteus.tracker.data.model.GoogleBookItem
import com.pluteus.tracker.data.model.MediaItem
import com.pluteus.tracker.data.model.MediaStatus
import com.pluteus.tracker.data.model.MediaType
import com.pluteus.tracker.data.model.TMDBResult
import com.pluteus.tracker.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddViewModel(private val repository: MediaRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    fun setSelectedType(type: MediaType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun setCreator(creator: String) {
        _uiState.value = _uiState.value.copy(creator = creator)
    }

    fun setStatus(status: MediaStatus) {
        _uiState.value = _uiState.value.copy(status = status)
    }

    fun setRating(rating: Float?) {
        _uiState.value = _uiState.value.copy(rating = rating)
    }

    fun setReview(review: String) {
        _uiState.value = _uiState.value.copy(review = review)
    }

    fun setTotalEpisodes(episodes: Int) {
        _uiState.value = _uiState.value.copy(totalEpisodes = episodes)
    }

    fun setWatchedEpisodes(episodes: Int) {
        _uiState.value = _uiState.value.copy(watchedEpisodes = episodes)
    }

    fun setTotalPages(pages: Int) {
        _uiState.value = _uiState.value.copy(totalPages = pages)
    }

    fun setReadPages(pages: Int) {
        _uiState.value = _uiState.value.copy(readPages = pages)
    }

    fun setImageUrl(url: String?) {
        _uiState.value = _uiState.value.copy(imageUrl = url)
    }

    fun setSelectedImageUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun setEditMode(editMode: Boolean) {
        _uiState.value = _uiState.value.copy(editMode = editMode)
    }

    fun setEditingItem(item: MediaItem?) {
        _uiState.value = _uiState.value.copy(
            editMode = item != null,
            editingItemId = item?.id,
            selectedType = item?.type ?: MediaType.MOVIE,
            title = item?.title ?: "",
            creator = item?.creator ?: "",
            status = item?.status ?: MediaStatus.PLANNED,
            imageUrl = item?.imageUrl,
            rating = item?.rating,
            review = item?.review ?: "",
            totalEpisodes = item?.totalEpisodes ?: 0,
            watchedEpisodes = item?.watchedEpisodes ?: 0,
            totalPages = item?.totalPages ?: 0,
            readPages = item?.readPages ?: 0
        )
    }

    fun searchTMDB(apiKey: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val query = _uiState.value.searchQuery
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            
            val result = repository.searchTMDB(query, apiKey)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                searchResults = result.getOrDefault(emptyList()),
                error = result.exceptionOrNull()?.message
            )
        }
    }

    fun searchGoogleBooks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val query = _uiState.value.searchQuery
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            
            val result = repository.searchGoogleBooks(query)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                bookSearchResults = result.getOrDefault(emptyList()),
                error = result.exceptionOrNull()?.message
            )
        }
    }

    fun selectTMDBResult(result: TMDBResult, type: MediaType) {
        _uiState.value = _uiState.value.copy(
            title = result.displayTitle,
            imageUrl = result.imageUrl,
            totalEpisodes = if (type == MediaType.SERIES) 24 else null
        )
    }

    fun selectBookResult(book: GoogleBookItem) {
        _uiState.value = _uiState.value.copy(
            title = book.volumeInfo.title,
            creator = book.volumeInfo.author ?: "",
            imageUrl = book.volumeInfo.imageUrl,
            totalPages = book.volumeInfo.pageCount ?: 0
        )
    }

    fun saveMediaItem() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            
            val currentState = _uiState.value
            
            // Validate required fields
            if (currentState.title.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Название обязательно для заполнения"
                )
                return@launch
            }

            val mediaItem = MediaItem(
                id = currentState.editingItemId ?: 0,
                title = currentState.title.trim(),
                creator = currentState.creator.trim(),
                type = currentState.selectedType,
                status = currentState.status,
                imageUrl = currentState.imageUrl,
                rating = currentState.rating,
                review = currentState.review.trim(),
                totalEpisodes = if (currentState.selectedType == MediaType.SERIES) currentState.totalEpisodes.takeIf { it > 0 } else null,
                watchedEpisodes = if (currentState.selectedType == MediaType.SERIES) currentState.watchedEpisodes else 0,
                totalPages = if (currentState.selectedType == MediaType.BOOK) currentState.totalPages.takeIf { it > 0 } else null,
                readPages = if (currentState.selectedType == MediaType.BOOK) currentState.readPages else 0
            )

            val result = if (currentState.editMode && currentState.editingItemId != null) {
                repository.updateMediaItem(mediaItem)
                Result.success(currentState.editingItemId)
            } else {
                repository.insertMediaItem(mediaItem)
            }

            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveSuccessful = result.isSuccess
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = AddUiState()
    }
}

data class AddUiState(
    val selectedType: MediaType = MediaType.MOVIE,
    val searchQuery: String = "",
    val title: String = "",
    val creator: String = "",
    val status: MediaStatus = MediaStatus.PLANNED,
    val imageUrl: String? = null,
    val selectedImageUri: Uri? = null,
    val rating: Float? = null,
    val review: String = "",
    val totalEpisodes: Int = 0,
    val watchedEpisodes: Int = 0,
    val totalPages: Int = 0,
    val readPages: Int = 0,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccessful: Boolean = false,
    val editMode: Boolean = false,
    val editingItemId: Long? = null,
    val searchResults: List<TMDBResult> = emptyList(),
    val bookSearchResults: List<GoogleBookItem> = emptyList(),
    val error: String? = null
)
