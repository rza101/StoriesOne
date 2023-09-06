package com.rhezarijaya.storiesone.di

import android.content.Context
import com.rhezarijaya.storiesone.data.StoryRepository
import com.rhezarijaya.storiesone.data.UserRepository
import com.rhezarijaya.storiesone.data.datastore.AppPreference
import com.rhezarijaya.storiesone.data.datastore.dataStore
import com.rhezarijaya.storiesone.data.network.APIConfig
import com.rhezarijaya.storiesone.data.network.service.StoryAPIService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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

    fun provideStoryRepository(context: Context) =
        StoryRepository(provideAPIService(context))

    fun provideUserRepository(context: Context) =
        UserRepository(provideAPIService(context), provideAppPreference(context))
}