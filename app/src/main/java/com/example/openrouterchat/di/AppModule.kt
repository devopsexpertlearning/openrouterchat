package com.example.openrouterchat.di

import android.content.Context
import androidx.room.Room
import com.example.openrouterchat.AppDatabase
import com.example.openrouterchat.ChatHistoryDao
import com.example.openrouterchat.ChatRepository
import com.example.openrouterchat.OpenRouterApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "chat-database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideChatHistoryDao(database: AppDatabase): ChatHistoryDao {
        return database.chatHistoryDao()
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        api: OpenRouterApi,
        database: AppDatabase
    ): ChatRepository {
        return ChatRepository(api, database.chatHistoryDao())
    }
}
