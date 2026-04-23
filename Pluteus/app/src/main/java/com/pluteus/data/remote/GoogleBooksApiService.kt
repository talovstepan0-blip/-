package com.pluteus.data.remote

import com.pluteus.domain.model.GoogleBooksSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApiService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("langRestrict") langRestrict: String? = null
    ): GoogleBooksSearchResponse
}
