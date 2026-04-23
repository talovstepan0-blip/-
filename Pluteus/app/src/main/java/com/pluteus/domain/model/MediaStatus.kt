package com.pluteus.domain.model

enum class MediaStatus(val displayName: String) {
    // Movie statuses
    PLANNING_MOVIE("Планирую"),
    WATCHING_MOVIE("Смотрю"),
    COMPLETED_MOVIE("Просмотрено"),
    
    // Series statuses
    PLANNING_SERIES("Планирую"),
    WATCHING_SERIES("Смотрю"),
    COMPLETED_SERIES("Просмотрено"),
    
    // Book statuses
    PLANNING_BOOK("Планирую"),
    READING_BOOK("Читаю"),
    COMPLETED_BOOK("Прочитано");
    
    companion object {
        fun fromTypeAndIndex(type: MediaType, index: Int): MediaStatus {
            return when (type) {
                MediaType.MOVIE -> when (index) {
                    0 -> PLANNING_MOVIE
                    1 -> WATCHING_MOVIE
                    2 -> COMPLETED_MOVIE
                    else -> PLANNING_MOVIE
                }
                MediaType.SERIES -> when (index) {
                    0 -> PLANNING_SERIES
                    1 -> WATCHING_SERIES
                    2 -> COMPLETED_SERIES
                    else -> PLANNING_SERIES
                }
                MediaType.BOOK -> when (index) {
                    0 -> PLANNING_BOOK
                    1 -> READING_BOOK
                    2 -> COMPLETED_BOOK
                    else -> PLANNING_BOOK
                }
            }
        }
        
        fun getStatusesForType(type: MediaType): List<MediaStatus> {
            return when (type) {
                MediaType.MOVIE -> listOf(PLANNING_MOVIE, WATCHING_MOVIE, COMPLETED_MOVIE)
                MediaType.SERIES -> listOf(PLANNING_SERIES, WATCHING_SERIES, COMPLETED_SERIES)
                MediaType.BOOK -> listOf(PLANNING_BOOK, READING_BOOK, COMPLETED_BOOK)
            }
        }
    }
}
