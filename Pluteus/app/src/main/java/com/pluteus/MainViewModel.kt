package com.pluteus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pluteus.data.repository.MediaRepository
import com.pluteus.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MediaRepository) : ViewModel() {
    
    private val _items = MutableStateFlow<List<MediaItem>>(emptyList())
    val items: StateFlow<List<MediaItem>> = _items.asStateFlow()
    
    init {
        CoroutineScope(Dispatchers.Main).launch {
            repository.getAllItems().collect { itemList ->
                _items.value = itemList
            }
        }
    }
    
    fun saveItem(item: MediaItem) {
        CoroutineScope(Dispatchers.IO).launch {
            if (item.id == 0L) {
                repository.insertItem(item)
            } else {
                repository.updateItem(item)
            }
        }
    }
    
    fun deleteItem(item: MediaItem) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteItem(item)
        }
    }
    
    fun getItemById(id: Long): Flow<MediaItem?> {
        return repository.getItemByIdFlow(id)
    }
}

class MainViewModelFactory(private val repository: MediaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
