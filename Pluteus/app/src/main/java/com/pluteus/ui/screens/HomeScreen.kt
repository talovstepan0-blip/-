package com.pluteus.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.pluteus.domain.model.MediaItem
import com.pluteus.domain.model.MediaType
import com.pluteus.domain.model.MediaStatus
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    items: List<MediaItem>,
    onItemSelected: (MediaItem) -> Unit,
    onAddClick: () -> Unit,
    onDeleteItem: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf<MediaType?>(null) }
    var selectedStatus by remember { mutableStateOf<MediaStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    val filteredItems = remember(items, selectedType, selectedStatus, searchQuery) {
        items.filter { item ->
            val typeMatch = selectedType == null || item.type == selectedType
            val statusMatch = selectedStatus == null || item.status == selectedStatus
            val searchMatch = searchQuery.isBlank() || 
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.creator.contains(searchQuery, ignoreCase = true)
            typeMatch && statusMatch && searchMatch
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pluteus") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтр")
                    }
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск по названию или автору") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Filter chips row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = selectedType == MediaType.MOVIE,
                    onClick = { selectedType = MediaType.MOVIE },
                    label = { Text("Фильмы") }
                )
                FilterChip(
                    selected = selectedType == MediaType.SERIES,
                    onClick = { selectedType = MediaType.SERIES },
                    label = { Text("Сериалы") }
                )
                FilterChip(
                    selected = selectedType == MediaType.BOOK,
                    onClick = { selectedType = MediaType.BOOK },
                    label = { Text("Книги") }
                )
            }
            
            // Items list
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    MediaItemCard(
                        item = item,
                        onClick = { onItemSelected(item) },
                        onDelete = { onDeleteItem(item) }
                    )
                }
                
                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Ничего не найдено",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showFilterDialog) {
        FilterDialog(
            selectedType = selectedType,
            selectedStatus = selectedStatus,
            onTypeSelected = { selectedType = it },
            onStatusSelected = { selectedStatus = it },
            onClear = {
                selectedType = null
                selectedStatus = null
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
private fun MediaItemCard(
    item: MediaItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (item.coverImageLocalPath != null) {
                    val file = File(item.coverImageLocalPath)
                    if (file.exists()) {
                        Image(
                            bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else if (!item.coverImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.coverImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.type) {
                                MediaType.MOVIE -> Icons.Default.Movie
                                MediaType.SERIES -> Icons.Default.Tv
                                MediaType.BOOK -> Icons.Default.Book
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = item.creator,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(item.status.displayName, style = MaterialTheme.typography.labelSmall) }
                    )
                    
                    if (item.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(item.rating),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                // Progress bar
                if (item.progressTotal > 0 || item.type != MediaType.MOVIE) {
                    Column {
                        LinearProgressIndicator(
                            progress = item.progressPercent / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.getProgressLabel(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
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
private fun FilterDialog(
    selectedType: MediaType?,
    selectedStatus: MediaStatus?,
    onTypeSelected: (MediaType?) -> Unit,
    onStatusSelected: (MediaStatus?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтр") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type filter
                Column {
                    Text(
                        text = "Тип",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { onTypeSelected(null) },
                            label = { Text("Все") }
                        )
                        FilterChip(
                            selected = selectedType == MediaType.MOVIE,
                            onClick = { onTypeSelected(MediaType.MOVIE) },
                            label = { Text("Фильмы") }
                        )
                        FilterChip(
                            selected = selectedType == MediaType.SERIES,
                            onClick = { onTypeSelected(MediaType.SERIES) },
                            label = { Text("Сериалы") }
                        )
                        FilterChip(
                            selected = selectedType == MediaType.BOOK,
                            onClick = { onTypeSelected(MediaType.BOOK) },
                            label = { Text("Книги") }
                        )
                    }
                }
                
                // Status filter
                Column {
                    Text(
                        text = "Статус",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { onStatusSelected(null) },
                            label = { Text("Все") }
                        )
                        FilterChip(
                            selected = selectedStatus?.name?.contains("PLANNING") == true,
                            onClick = { onStatusSelected(MediaStatus.PLANNING_MOVIE) },
                            label = { Text("Планирую") }
                        )
                        FilterChip(
                            selected = selectedStatus?.name?.contains("WATCHING") == true || 
                                     selectedStatus?.name?.contains("READING") == true,
                            onClick = { onStatusSelected(MediaStatus.WATCHING_MOVIE) },
                            label = { Text("В процессе") }
                        )
                        FilterChip(
                            selected = selectedStatus?.name?.contains("COMPLETED") == true,
                            onClick = { onStatusSelected(MediaStatus.COMPLETED_MOVIE) },
                            label = { Text("Завершено") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onClear()
                onDismiss()
            }) {
                Text("Сбросить")
            }
        }
    )
}
