package com.pluteus.domain.model

data class GoogleBooksSearchResponse(
    val kind: String?,
    val totalItems: Int,
    val items: List<GoogleBookItem>?
)

data class GoogleBookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val subtitle: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val pageCount: Int?,
    val categories: List<String>?,
    val imageLinks: ImageLinks?,
    val language: String?,
    val averageRating: Double?,
    val ratingsCount: Int?
) {
    val imageUrl: String?
        get() = imageLinks?.thumbnail ?: imageLinks?.smallThumbnail
    
    val authorName: String
        get() = authors?.joinToString(", ") ?: "Unknown"
}

data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?,
    val small: String?,
    val medium: String?,
    val large: String?,
    val extraLarge: String?
)
