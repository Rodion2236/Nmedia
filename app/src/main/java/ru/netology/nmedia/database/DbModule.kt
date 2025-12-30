package ru.netology.nmedia.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.dao.PostDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext
        context: Context
    ): PostDatabase = Room.databaseBuilder(
        context.applicationContext,
        PostDatabase::class.java,
        "posts.db"
    )
        .fallbackToDestructiveMigration(false)
        .build()

    @Provides
    fun providePostDao(postDatabase: PostDatabase): PostDao = postDatabase.postDao()
}