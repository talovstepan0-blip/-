package com.pluteus.tracker.data.model

import com.google.gson.annotations.SerializedName

data class TMDBSearchResponse(
    @SerializedName("results") val results: List<TMDBResult>,
    @SerializedName("total_results") val totalResults: Int
)

data class TMDBResult(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("media_type") val mediaType: String?
) {
    val displayTitle: String get() = title ?: name ?: "Без названия"
    val imageUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

data class TMDBDetailsResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int?,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int?,
    @SerializedName("genres") val genres: List<Genre>?,
    @SerializedName("credits") val credits: Credits?
) {
    val displayTitle: String get() = title ?: name ?: "Без названия"
    val director: String?
        get() = credits?.crew?.find { it.job == "Director" }?.name
    val imageUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class Credits(
    @SerializedName("cast") val cast: List<Cast>?,
    @SerializedName("crew") val crew: List<Crew>?
)

data class Cast(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String
)

data class Crew(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("job") val job: String
)
