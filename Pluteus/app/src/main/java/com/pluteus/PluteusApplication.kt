package com.pluteus

import android.app.Application
import com.pluteus.data.local.AppDatabase
import com.pluteus.data.repository.MediaRepository

class PluteusApplication : Application() {
    
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
    
    val repository: MediaRepository by lazy {
        MediaRepository(database.mediaItemDao())
    }
}
