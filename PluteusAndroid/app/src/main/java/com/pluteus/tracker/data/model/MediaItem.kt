package com.pluteus.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val creator: String,
    val type: MediaType,
    val status: MediaStatus,
    val imageUrl: String? = null,
    val imageUrlLocal: String? = null,
    val rating: Float? = null,
    val review: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // Для сериалов
    val totalEpisodes: Int? = null,
    val watchedEpisodes: Int = 0,
    // Для книг
    val totalPages: Int? = null,
    val readPages: Int = 0
) {
    val progressPercent: Float
        get() = when (type) {
            MediaType.SERIES -> {
                if (totalEpisodes == null || totalEpisodes == 0) 0f
                else (watchedEpisodes.toFloat() / totalEpisodes) * 100f
            }
            MediaType.BOOK -> {
                if (totalPages == null || totalPages == 0) 0f
                else (readPages.toFloat() / totalPages) * 100f
            }
            MediaType.MOVIE -> if (status == MediaStatus.COMPLETED_MOVIE || status == MediaStatus.COMPLETED_BOOK) 100f else 0f
        }
}

enum class MediaType {
    MOVIE, SERIES, BOOK
}

enum class MediaStatus(val displayName: String) {
    PLANNED("Планирую"),
    WATCHING("Смотрю"),
    READING("Читаю"),
    COMPLETED_MOVIE("Просмотрено"),
    COMPLETED_BOOK("Прочитано");

    companion object {
        fun getStatusesForType(type: MediaType): List<MediaStatus> = when (type) {
            MediaType.MOVIE -> listOf(PLANNED, WATCHING, COMPLETED_MOVIE)
            MediaType.SERIES -> listOf(PLANNED, WATCHING, COMPLETED_MOVIE)
            MediaType.BOOK -> listOf(PLANNED, READING, COMPLETED_BOOK)
        }
    }
}
