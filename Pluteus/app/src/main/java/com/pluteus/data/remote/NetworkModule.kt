package com.pluteus.data.remote

import com.pluteus.util.ApiConstants
import com.pluteus.util.ApiKeys
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()
    
    private val gsonFactory = GsonConverterFactory.create()
    
    val tmdbApi: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.TMDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonFactory)
            .build()
            .create(TmdbApiService::class.java)
    }
    
    val googleBooksApi: GoogleBooksApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiKeys.GOOGLE_BOOKS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonFactory)
            .build()
            .create(GoogleBooksApiService::class.java)
    }
}
