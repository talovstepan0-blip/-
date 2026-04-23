package com.pluteus.tracker.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.pluteus.tracker.data.model.GoogleBookItem
import com.pluteus.tracker.data.model.MediaItem
import com.pluteus.tracker.data.model.MediaStatus
import com.pluteus.tracker.data.model.MediaType
import com.pluteus.tracker.data.repository.MediaRepository
import java.io.File
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pluteus.tracker.ui.components.RatingBar
import com.pluteus.tracker.ui.components.StatusBadge
import com.pluteus.tracker.ui.components.TypeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    onNavigateBack: () -> Unit,
    onItemSaved: () -> Unit,
    editingItem: MediaItem? = null,
    repository: MediaRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: AddViewModel = viewModel(factory = AddViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val tmdbApiKey = context.getString(com.pluteus.tracker.R.string.tmdb_api_key)

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setSelectedImageUri(uri)
        uri?.let {
            // Можно загрузить изображение и сохранить локально
        }
    }

    LaunchedEffect(editingItem) {
        if (editingItem != null) {
            viewModel.setEditingItem(editingItem)
        }
    }

    LaunchedEffect(uiState.saveSuccessful) {
        if (uiState.saveSuccessful) {
            onItemSaved()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.editMode) "Редактировать" else "Добавить") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                },
                actions = {
                    if (uiState.editMode) {
                        IconButton(onClick = {
                            viewModel.saveMediaItem()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Сохранить")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type selector
            TypeSelector(
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::setSelectedType,
                enabled = !uiState.editMode
            )

            // Search field
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Поиск через API", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                when (uiState.selectedType) {
                                    MediaType.MOVIE, MediaType.SERIES -> viewModel.searchTMDB(tmdbApiKey)
                                    MediaType.BOOK -> viewModel.searchGoogleBooks()
                                }
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "Поиск")
                            }
                        }
                    )
                }
            }

            // Search results
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // TMDB results
                if (uiState.searchResults.isNotEmpty() && uiState.selectedType != MediaType.BOOK) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Результаты поиска:", fontWeight = FontWeight.Bold)
                            LazyColumn {
                                items(uiState.searchResults) { result ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectTMDBResult(result, uiState.selectedType)
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        result.imageUrl?.let { url ->
                                            AsyncImage(
                                                model = url,
                                                contentDescription = result.displayTitle,
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(result.displayTitle)
                                    }
                                }
                            }
                        }
                    }
                }

                // Google Books results
                if (uiState.bookSearchResults.isNotEmpty() && uiState.selectedType == MediaType.BOOK) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Результаты поиска:", fontWeight = FontWeight.Bold)
                            LazyColumn {
                                items(uiState.bookSearchResults) { book ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectBookResult(book)
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        book.volumeInfo.imageUrl?.let { url ->
                                            AsyncImage(
                                                model = url,
                                                contentDescription = book.volumeInfo.title,
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Column {
                                            Text(book.volumeInfo.title, fontWeight = FontWeight.Bold)
                                            book.volumeInfo.author?.let { author ->
                                                Text(author, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Image preview and picker
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Обложка", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.imageUrl != null || uiState.selectedImageUri != null) {
                            AsyncImage(
                                model = uiState.selectedImageUri ?: uiState.imageUrl,
                                contentDescription = "Обложка",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Text("Выбрать фото")
                        }
                    }
                }
            }

            // Title field
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::setTitle,
                label = { Text("Название *") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.title.isBlank() && uiState.error != null
            )

            // Creator field
            OutlinedTextField(
                value = uiState.creator,
                onValueChange = viewModel::setCreator,
                label = { Text("Автор / Режиссёр") },
                modifier = Modifier.fillMaxWidth()
            )

            // Status selector
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Статус", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val availableStatuses = MediaStatus.getStatusesForType(uiState.selectedType)
                    availableStatuses.forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setStatus(status) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.status == status,
                                onClick = { viewModel.setStatus(status) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(status.displayName)
                        }
                    }
                }
            }

            // Progress fields based on type
            when (uiState.selectedType) {
                MediaType.SERIES -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Прогресс сериала", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.totalEpisodes.toString(),
                                onValueChange = { viewModel.setTotalEpisodes(it.toIntOrNull() ?: 0) },
                                label = { Text("Всего серий") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.watchedEpisodes.toString(),
                                onValueChange = { viewModel.setWatchedEpisodes(it.toIntOrNull() ?: 0) },
                                label = { Text("Просмотрено серий") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (uiState.totalEpisodes > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = uiState.watchedEpisodes.toFloat() / uiState.totalEpisodes,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "${((uiState.watchedEpisodes.toFloat() / uiState.totalEpisodes) * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                MediaType.BOOK -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Прогресс книги", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.totalPages.toString(),
                                onValueChange = { viewModel.setTotalPages(it.toIntOrNull() ?: 0) },
                                label = { Text("Всего страниц") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.readPages.toString(),
                                onValueChange = { viewModel.setReadPages(it.toIntOrNull() ?: 0) },
                                label = { Text("Прочитано страниц") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (uiState.totalPages > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = uiState.readPages.toFloat() / uiState.totalPages,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "${((uiState.readPages.toFloat() / uiState.totalPages) * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                MediaType.MOVIE -> {
                    // Для фильмов прогресс не требуется или бинарный
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Фильм просмотрен?", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.status == MediaStatus.COMPLETED_MOVIE,
                                    onClick = { viewModel.setStatus(MediaStatus.COMPLETED_MOVIE) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Да, просмотрен")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.status != MediaStatus.COMPLETED_MOVIE,
                                    onClick = { 
                                        if (uiState.status == MediaStatus.PLANNED) {
                                            viewModel.setStatus(MediaStatus.WATCHING)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Нет")
                            }
                        }
                    }
                }
            }

            // Rating
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Оценка", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    RatingBar(
                        rating = uiState.rating ?: 0f,
                        onRatingChanged = viewModel::setRating
                    )
                }
            }

            // Review
            OutlinedTextField(
                value = uiState.review,
                onValueChange = viewModel::setReview,
                label = { Text("Отзыв") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Save button
            Button(
                onClick = { viewModel.saveMediaItem() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isSaving && uiState.title.isNotBlank()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Сохранить", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ViewModel Factory
class AddViewModelFactory(
    private val repository: MediaRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return AddViewModel(repository) as T
    }
}
