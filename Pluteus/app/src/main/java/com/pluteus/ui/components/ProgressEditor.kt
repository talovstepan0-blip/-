package com.pluteus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun ProgressEditor(
    currentProgress: Int,
    totalProgress: Int,
    onCurrentChanged: (Int) -> Unit,
    onTotalChanged: (Int) -> Unit,
    unitLabel: String,
    modifier: Modifier = Modifier,
    showSlider: Boolean = true
) {
    var currentText by remember { mutableStateOf(currentProgress.toString()) }
    var totalText by remember { mutableStateOf(totalProgress.toString()) }
    
    LaunchedEffect(currentProgress) {
        currentText = currentProgress.toString()
    }
    
    LaunchedEffect(totalProgress) {
        totalText = totalProgress.toString()
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Прогресс ($unitLabel)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        if (showSlider && totalProgress > 0) {
            val progressPercent = if (totalProgress > 0) currentProgress.toFloat() / totalProgress else 0f
            
            Slider(
                value = currentProgress.toFloat(),
                onValueChange = { newValue ->
                    val newIntValue = newValue.toInt()
                    currentText = newIntValue.toString()
                    onCurrentChanged(newIntValue)
                },
                valueRange = 0f..totalProgress.toFloat(),
                steps = totalProgress - 1,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "$currentProgress/$totalProgress $unitLabel (${(progressPercent * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentText,
                onValueChange = { newText ->
                    currentText = newText
                    newText.toIntOrNull()?.let { onCurrentChanged(it) }
                },
                label = { Text("Просмотрено") },
                placeholder = { Text("0") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            
            OutlinedTextField(
                value = totalText,
                onValueChange = { newText ->
                    totalText = newText
                    newText.toIntOrNull()?.let { onTotalChanged(it) }
                },
                label = { Text("Всего") },
                placeholder = { Text("0") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
