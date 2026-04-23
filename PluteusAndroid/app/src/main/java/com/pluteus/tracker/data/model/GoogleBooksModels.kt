package com.pluteus.tracker.data.model

import com.google.gson.annotations.SerializedName

data class GoogleBooksResponse(
    @SerializedName("items") val items: List<GoogleBookItem>?,
    @SerializedName("totalItems") val totalItems: Int
)

data class GoogleBookItem(
    @SerializedName("id") val id: String,
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    @SerializedName("title") val title: String,
    @SerializedName("authors") val authors: List<String>?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("publishedDate") val publishedDate: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("pageCount") val pageCount: Int?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("imageLinks") val imageLinks: ImageLinks?,
    @SerializedName("language") val language: String?,
    @SerializedName("averageRating") val averageRating: Float?,
    @SerializedName("ratingsCount") val ratingsCount: Int?
) {
    val author: String? get() = authors?.joinToString(", ")
    val imageUrl: String? get() = imageLinks?.thumbnail ?: imageLinks?.smallThumbnail
}

data class ImageLinks(
    @SerializedName("smallThumbnail") val smallThumbnail: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("small") val small: String?,
    @SerializedName("medium") val medium: String?,
    @SerializedName("large") val large: String?
)
