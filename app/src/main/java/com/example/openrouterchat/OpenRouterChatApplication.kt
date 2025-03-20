package com.example.openrouterchat

import android.app.Application
import androidx.room.Room
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenRouterChatApplication : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "chat-database"
        )
        .fallbackToDestructiveMigration() // Add this line for development
        .build()
    }
}
