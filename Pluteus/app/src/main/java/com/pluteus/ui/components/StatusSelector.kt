package com.pluteus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pluteus.domain.model.MediaStatus
import com.pluteus.domain.model.MediaType

@Composable
fun StatusSelector(
    mediaType: MediaType,
    currentStatus: MediaStatus,
    onStatusChanged: (MediaStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val statuses = MediaStatus.getStatusesForType(mediaType)
    
    Column(modifier = modifier) {
        Text(
            text = "Статус",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            statuses.forEach { status ->
                FilterChip(
                    selected = currentStatus == status,
                    onClick = { onStatusChanged(status) },
                    label = { Text(status.displayName) },
                    leadingIcon = if (currentStatus == status) {
                        @Composable {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}
