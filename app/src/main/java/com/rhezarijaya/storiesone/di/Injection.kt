package com.rhezarijaya.storiesone.di

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import com.rhezarijaya.storiesone.data.StoryRepository
import com.rhezarijaya.storiesone.data.UserRepository
import com.rhezarijaya.storiesone.data.datastore.AppPreference
import com.rhezarijaya.storiesone.data.datastore.dataStore
import com.rhezarijaya.storiesone.data.network.APIConfig
import com.rhezarijaya.storiesone.data.network.service.StoryAPIService
import com.rhezarijaya.storiesone.data.room.StoriesDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@ExperimentalPagingApi
object Injection {
    private fun provideAppPreference(context: Context): AppPreference =
        AppPreference.getInstance(context.dataStore)

    private fun provideAPIService(context: Context): StoryAPIService {
        val appPreference = provideAppPreference(context)
        val bearerToken = runBlocking {
            appPreference.getToken().first()
        }

        return APIConfig.getStoryAPIService(bearerToken ?: "")
    }

    private fun provideStoriesDatabase(context: Context): StoriesDatabase {
        return StoriesDatabase.getInstance(context)
    }

    fun provideStoryRepository(context: Context) =
        StoryRepository(provideStoriesDatabase(context), provideAPIService(context))

    fun provideUserRepository(context: Context) =
        UserRepository(provideAPIService(context), provideAppPreference(context))
}