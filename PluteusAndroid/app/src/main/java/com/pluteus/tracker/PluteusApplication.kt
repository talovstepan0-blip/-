package com.pluteus.tracker

import android.app.Application
import com.pluteus.tracker.data.repository.MediaRepository

class PluteusApplication : Application() {
    lateinit var repository: MediaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = MediaRepository(this)
    }
}
