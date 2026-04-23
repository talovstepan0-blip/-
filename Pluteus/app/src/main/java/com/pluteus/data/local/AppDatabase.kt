package com.pluteus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pluteus.domain.model.MediaItem
import com.pluteus.domain.model.MediaType
import com.pluteus.domain.model.MediaStatus

@Database(entities = [MediaItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pluteus_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromMediaType(value: MediaType): String {
        return value.name
    }
    
    @TypeConverter
    fun toMediaType(value: String): MediaType {
        return MediaType.valueOf(value)
    }
    
    @TypeConverter
    fun fromMediaStatus(value: MediaStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toMediaStatus(value: String): MediaStatus {
        return MediaStatus.valueOf(value)
    }
}
