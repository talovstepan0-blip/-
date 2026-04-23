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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pluteus.domain.model.*
import com.pluteus.ui.components.ProgressEditor
import com.pluteus.ui.components.RatingSelector
import com.pluteus.ui.components.StatusSelector
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    item: MediaItem?,
    onSave: (MediaItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var creator by remember { mutableStateOf(item?.creator ?: "") }
    var type by remember { mutableStateOf(item?.type ?: MediaType.MOVIE) }
    var status by remember { mutableStateOf(item?.status ?: MediaStatus.PLANNING_MOVIE) }
    var progressCurrent by remember { mutableStateOf(item?.progressCurrent ?: 0) }
    var progressTotal by remember { mutableStateOf(item?.progressTotal ?: 0) }
    var rating by remember { mutableStateOf(item?.rating ?: 0f) }
    var review by remember { mutableStateOf(item?.review ?: "") }
    var coverImageUrl by remember { mutableStateOf(item?.coverImageUrl) }
    var coverImageLocalPath by remember { mutableStateOf(item?.coverImageLocalPath) }
    
    var showSearchDialog by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Save image to local storage
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val outputFile = File(context.filesDir, "covers/${System.currentTimeMillis()}.jpg")
                outputFile.parentFile?.mkdirs()
                
                inputStream?.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                coverImageLocalPath = outputFile.absolutePath
                coverImageUrl = null // Clear URL when using local image
            } catch (e: Exception) {
                showError = "Ошибка загрузки изображения: ${e.message}"
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (item == null) "Добавить" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                val mediaItem = item?.copy(
                                    title = title,
                                    creator = creator,
                                    type = type,
                                    status = status,
                                    progressCurrent = progressCurrent,
                                    progressTotal = progressTotal,
                                    rating = rating,
                                    review = review,
                                    coverImageUrl = coverImageUrl,
                                    coverImageLocalPath = coverImageLocalPath,
                                    lastModified = System.currentTimeMillis()
                                ) ?: MediaItem(
                                    title = title,
                                    creator = creator,
                                    type = type,
                                    status = status,
                                    progressCurrent = progressCurrent,
                                    progressTotal = progressTotal,
                                    rating = rating,
                                    review = review,
                                    coverImageUrl = coverImageUrl,
                                    coverImageLocalPath = coverImageLocalPath
                                )
                                onSave(mediaItem)
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Сохранить")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type selector
            item {
                Column {
                    Text(
                        text = "Тип",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == MediaType.MOVIE,
                            onClick = { 
                                type = MediaType.MOVIE
                                status = MediaStatus.PLANNING_MOVIE
                            },
                            label = { Text("Фильм") },
                            leadingIcon = if (type == MediaType.MOVIE) {
                                @Composable { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = type == MediaType.SERIES,
                            onClick = { 
                                type = MediaType.SERIES
                                status = MediaStatus.PLANNING_SERIES
                            },
                            label = { Text("Сериал") },
                            leadingIcon = if (type == MediaType.SERIES) {
                                @Composable { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = type == MediaType.BOOK,
                            onClick = { 
                                type = MediaType.BOOK
                                status = MediaStatus.PLANNING_BOOK
                            },
                            label = { Text("Книга") },
                            leadingIcon = if (type == MediaType.BOOK) {
                                @Composable { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }
            
            // Cover image section
            item {
                Column {
                    Text(
                        text = "Обложка",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (coverImageLocalPath != null && File(coverImageLocalPath!!).exists()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(File(coverImageLocalPath!!))
                                        .build(),
                                    contentDescription = "Обложка",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else if (!coverImageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(coverImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Обложка",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Добавить фото",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showSearchDialog = true },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Найти онлайн")
                            }
                            
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Из галереи")
                            }
                            
                            if (coverImageUrl != null || coverImageLocalPath != null) {
                                OutlinedButton(
                                    onClick = {
                                        coverImageUrl = null
                                        coverImageLocalPath = null
                                    },
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Удалить")
                                }
                            }
                        }
                    }
                }
            }
            
            // Title field
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название *") },
                    placeholder = { Text("Введите название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank()
                )
            }
            
            // Creator field
            item {
                OutlinedTextField(
                    value = creator,
                    onValueChange = { creator = it },
                    label = { Text(if (type == MediaType.BOOK) "Автор" else "Режиссёр") },
                    placeholder = { Text("Введите имя") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Status selector
            item {
                StatusSelector(
                    mediaType = type,
                    currentStatus = status,
                    onStatusChanged = { status = it }
                )
            }
            
            // Progress editor
            item {
                when (type) {
                    MediaType.MOVIE -> {
                        // For movies, just show completed status indicator
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Просмотрен")
                                Switch(
                                    checked = status == MediaStatus.COMPLETED_MOVIE,
                                    onCheckedChange = { isChecked ->
                                        status = if (isChecked) MediaStatus.COMPLETED_MOVIE else MediaStatus.WATCHING_MOVIE
                                    }
                                )
                            }
                        }
                    }
                    MediaType.SERIES -> {
                        ProgressEditor(
                            currentProgress = progressCurrent,
                            totalProgress = progressTotal,
                            onCurrentChanged = { progressCurrent = it },
                            onTotalChanged = { progressTotal = it },
                            unitLabel = "серий"
                        )
                    }
                    MediaType.BOOK -> {
                        ProgressEditor(
                            currentProgress = progressCurrent,
                            totalProgress = progressTotal,
                            onCurrentChanged = { progressCurrent = it },
                            onTotalChanged = { progressTotal = it },
                            unitLabel = "страниц"
                        )
                    }
                }
            }
            
            // Rating selector
            item {
                RatingSelector(
                    rating = rating,
                    onRatingChanged = { rating = it }
                )
            }
            
            // Review field
            item {
                OutlinedTextField(
                    value = review,
                    onValueChange = { review = it },
                    label = { Text("Отзыв") },
                    placeholder = { Text("Ваши мысли о произведении...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )
            }
        }
    }
    
    // Search dialog for auto-fill
    if (showSearchDialog) {
        ApiSearchDialog(
            mediaType = type,
            onItemSelected = { result ->
                title = result.title
                creator = result.creator
                coverImageUrl = result.coverImageUrl
                if (result.progressTotal > 0) {
                    progressTotal = result.progressTotal
                }
                showSearchDialog = false
            },
            onDismiss = { showSearchDialog = false }
        )
    }
    
    // Error snackbar
    showError?.let { error ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showError = null }) {
                    Text("OK")
                }
            }
        ) {
            Text(error)
        }
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            showError = null
        }
    }
}

data class SearchResultData(
    val title: String,
    val creator: String,
    val coverImageUrl: String?,
    val progressTotal: Int
)

@Composable
private fun ApiSearchDialog(
    mediaType: MediaType,
    onItemSelected: (SearchResultData) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResultData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Поиск в интернете") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Название") },
                    placeholder = { Text("Введите название для поиска") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { 
                            // In a real app, this would call the API
                            // For now, just show mock results
                            isLoading = true
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(500)
                                searchResults = emptyList() // Mock empty results
                                isLoading = false
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Поиск")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Введите название и нажмите поиск",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { result ->
                            SearchResultItem(
                                result = result,
                                onClick = { onItemSelected(result) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun SearchResultItem(
    result: SearchResultData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (result.coverImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(result.coverImageUrl)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.creator,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
