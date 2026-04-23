package com.pluteus.tracker.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.pluteus.tracker.data.model.MediaItem
import com.pluteus.tracker.data.model.MediaStatus
import com.pluteus.tracker.data.model.MediaType
import com.pluteus.tracker.data.repository.MediaRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddItemClick: () -> Unit,
    onEditItemClick: (MediaItem) -> Unit,
    repository: MediaRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pluteus") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтр")
                    }
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Сортировка")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItemClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                label = { Text("Поиск...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterType == null,
                    onClick = { viewModel.setFilterType(null) },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = uiState.filterType == MediaType.MOVIE,
                    onClick = { viewModel.setFilterType(MediaType.MOVIE) },
                    label = { Text("Фильмы") }
                )
                FilterChip(
                    selected = uiState.filterType == MediaType.SERIES,
                    onClick = { viewModel.setFilterType(MediaType.SERIES) },
                    label = { Text("Сериалы") }
                )
                FilterChip(
                    selected = uiState.filterType == MediaType.BOOK,
                    onClick = { viewModel.setFilterType(MediaType.BOOK) },
                    label = { Text("Книги") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Media items list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.mediaItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Нет записей",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Нажмите + чтобы добавить",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.mediaItems, key = { it.id }) { item ->
                        MediaItemCard(
                            item = item,
                            imageFile = viewModel.getImageFile(item.id),
                            onClick = { onEditItemClick(item) },
                            onDeleteClick = { viewModel.deleteItem(item) },
                            onStatusChange = { newStatus -> viewModel.updateStatus(item, newStatus) }
                        )
                    }
                }
            }
        }
    }

    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            currentType = uiState.filterType,
            currentStatus = uiState.filterStatus,
            onTypeSelected = viewModel::setFilterType,
            onStatusSelected = viewModel::setFilterStatus,
            onDismiss = { showFilterDialog = false }
        )
    }

    // Sort dialog
    if (showSortDialog) {
        SortDialog(
            currentSortBy = uiState.sortBy,
            currentAscending = uiState.sortAscending,
            onSortBySelected = viewModel::setSortBy,
            onSortOrderChanged = viewModel::setSortAscending,
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
fun MediaItemCard(
    item: MediaItem,
    imageFile: File?,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStatusChange: (MediaStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            AsyncImage(
                model = imageFile ?: item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.creator,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = item.status.displayName)
                    
                    item.rating?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = androidx.compose.ui.graphics.Color(0xFFFFC107)
                            )
                            Text(
                                text = rating.toString(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Progress bar for series/books
                if (item.type == MediaType.SERIES || item.type == MediaType.BOOK) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = item.progressPercent / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${item.progressPercent.toInt()}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Delete button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun FilterDialog(
    currentType: MediaType?,
    currentStatus: MediaStatus?,
    onTypeSelected: (MediaType?) -> Unit,
    onStatusSelected: (MediaStatus?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтр") },
        text = {
            Column {
                Text("Тип:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = currentType == null,
                        onClick = { onTypeSelected(null) },
                        label = { Text("Все") }
                    )
                    MediaType.entries.forEach { type ->
                        FilterChip(
                            selected = currentType == type,
                            onClick = { onTypeSelected(type) },
                            label = { Text(getTypeName(type)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Статус:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = currentStatus == null,
                        onClick = { onStatusSelected(null) },
                        label = { Text("Все") }
                    )
                    MediaStatus.entries.forEach { status ->
                        FilterChip(
                            selected = currentStatus == status,
                            onClick = { onStatusSelected(status) },
                            label = { Text(status.displayName) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSortBy: SortBy,
    currentAscending: Boolean,
    onSortBySelected: (SortBy) -> Unit,
    onSortOrderChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сортировка") },
        text = {
            Column {
                Text("Сортировать по:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                SortBy.entries.forEach { sortBy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortBySelected(sortBy) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortBy == sortBy,
                            onClick = { onSortBySelected(sortBy) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getSortByName(sortBy))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Порядок:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !currentAscending,
                        onClick = { onSortOrderChanged(false) },
                        label = { Text("По убыванию") }
                    )
                    FilterChip(
                        selected = currentAscending,
                        onClick = { onSortOrderChanged(true) },
                        label = { Text("По возрастанию") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun getTypeName(type: MediaType): String = when (type) {
    MediaType.MOVIE -> "Фильмы"
    MediaType.SERIES -> "Сериалы"
    MediaType.BOOK -> "Книги"
}

private fun getSortByName(sortBy: SortBy): String = when (sortBy) {
    SortBy.DATE -> "Дата добавления"
    SortBy.NAME -> "Название"
    SortBy.RATING -> "Оценка"
}

// ViewModel Factory
class HomeViewModelFactory(
    private val repository: MediaRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository, LocalContext.current.applicationContext) as T
    }
}
