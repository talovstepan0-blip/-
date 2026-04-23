package com.pluteus.tracker.data.remote

import com.pluteus.tracker.data.model.GoogleBooksResponse
import com.pluteus.tracker.data.model.TMDBDetailsResponse
import com.pluteus.tracker.data.model.TMDBSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBApiService {
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "ru-RU",
        @Query("page") page: Int = 1
    ): TMDBSearchResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("append_to_response") appendToResponse: String = "credits"
    ): TMDBDetailsResponse

    @GET("tv/{tv_id}")
    suspend fun getTVDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ru-RU",
        @Query("append_to_response") appendToResponse: String = "credits"
    ): TMDBDetailsResponse
}

interface GoogleBooksApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10,
        @Query("langRestrict") langRestrict: String? = null,
        @Query("orderBy") orderBy: String? = "relevance"
    ): GoogleBooksResponse
}
