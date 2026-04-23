package com.pluteus.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: MediaType,
    val title: String,
    val creator: String,
    val status: MediaStatus,
    val progressCurrent: Int = 0,
    val progressTotal: Int = 0,
    val rating: Float = 0f,
    val review: String = "",
    val coverImageUrl: String? = null,
    val coverImageLocalPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (progressTotal > 0) {
            (progressCurrent.toFloat() / progressTotal) * 100f
        } else {
            when (type) {
                MediaType.MOVIE -> if (status == MediaStatus.COMPLETED_MOVIE) 100f else 0f
                MediaType.SERIES, MediaType.BOOK -> 0f
            }
        }
    
    fun getProgressLabel(): String {
        return when (type) {
            MediaType.MOVIE -> if (status == MediaStatus.COMPLETED_MOVIE) "Просмотрено" else "Не просмотрено"
            MediaType.SERIES -> "$progressCurrent/$progressTotal серий"
            MediaType.BOOK -> "$progressCurrent/$progressTotal страниц"
        }
    }
}
