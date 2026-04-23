package com.pluteus.tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pluteus.tracker.data.model.MediaType

@Composable
fun TypeSelector(
    selectedType: MediaType,
    onTypeSelected: (MediaType) -> Unit,
    enabled: Boolean = true
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Тип контента", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                MediaType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { if (enabled) onTypeSelected(type) },
                        label = { Text(getTypeName(type)) },
                        leadingIcon = if (selectedType == type) {
                            {
                                Icon(
                                    imageVector = getIconForType(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    AssistChip(
        onClick = { },
        label = { Text(status, style = MaterialTheme.typography.labelMedium) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    maxStars: Int = 5
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val starValue = i.toFloat()
            Icon(
                imageVector = if (rating >= starValue) Icons.Filled.Star else 
                           if (rating >= starValue - 0.5f) Icons.Filled.StarHalf else Icons.Filled.StarBorder,
                contentDescription = "Звезда $i",
                tint = if (rating >= starValue - 0.5f) Color(0xFFFFC107) else Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChanged(starValue) }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (rating > 0) "$rating" else "Нет оценки",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun getTypeName(type: MediaType): String = when (type) {
    MediaType.MOVIE -> "Фильм"
    MediaType.SERIES -> "Сериал"
    MediaType.BOOK -> "Книга"
}

private fun getIconForType(type: MediaType): ImageVector = when (type) {
    MediaType.MOVIE -> Icons.Default.Movie
    MediaType.SERIES -> Icons.Default.Tv
    MediaType.BOOK -> Icons.Default.Book
}
