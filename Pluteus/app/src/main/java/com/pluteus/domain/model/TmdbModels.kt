package com.pluteus.domain.model

data class TmdbSearchResult(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String?,
    val posterPath: String?,
    val releaseDate: String?,
    val firstAirDate: String?,
    val voteAverage: Double?,
    val mediaType: String?
) {
    val displayName: String
        get() = title ?: name ?: "Unknown"
    
    val displayDate: String?
        get() = releaseDate ?: firstAirDate
    
    val imageUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
}

data class TmdbDetailResponse(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String?,
    val posterPath: String?,
    val releaseDate: String?,
    val firstAirDate: String?,
    val voteAverage: Double?,
    val numberOfSeasons: Int?,
    val numberOfEpisodes: Int?,
    val genres: List<Genre>?,
    val creators: List<Creator>?
) {
    val displayName: String
        get() = title ?: name ?: "Unknown"
    
    val displayDate: String?
        get() = releaseDate ?: firstAirDate
    
    val imageUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    
    val totalEpisodes: Int
        get() = numberOfEpisodes ?: numberOfSeasons?.times(10) ?: 0
    
    val directorOrCreator: String
        get() = creators?.firstOrNull()?.name ?: "Unknown"
}

data class Genre(val id: Int, val name: String)
data class Creator(val id: Int, val name: String)
