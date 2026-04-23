package com.pluteus.util

object ApiKeys {
    // TODO: Замените на ваш API ключ TMDB
    // Получите ключ на https://www.themoviedb.org/settings/api
    const val TMDB_API_KEY = "YOUR_TMDB_API_KEY_HERE"
    
    // Google Books API не требует ключа для базового использования
    const val GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/"
}

object ApiConstants {
    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
