package com.pluteus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RatingSelector(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5
) {
    var isSettingRating by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = "Оценка",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxRating) {
                val starValue = i.toFloat()
                val halfStarValue = i - 0.5f
                
                IconButton(
                    onClick = {
                        if (rating == starValue) {
                            onRatingChanged((i - 1).toFloat())
                        } else {
                            onRatingChanged(starValue)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (rating >= starValue) {
                            Icons.Filled.Star
                        } else if (rating >= halfStarValue) {
                            Icons.Filled.StarHalf
                        } else {
                            Icons.Outlined.StarBorder
                        },
                        contentDescription = "$i звезд",
                        tint = if (rating >= halfStarValue) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (rating > 0) "%.1f".format(rating) else "Нет оценки",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
