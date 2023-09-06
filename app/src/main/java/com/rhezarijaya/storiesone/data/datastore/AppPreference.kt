package com.rhezarijaya.storiesone.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rhezarijaya.storiesone.data.datastore.AppPreference.Companion.PREFERENCE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)

class AppPreference private constructor(private val dataStore: DataStore<Preferences>) {
    private val nameKey = stringPreferencesKey("name")
    private val userIdKey = stringPreferencesKey("user_id")
    private val tokenKey = stringPreferencesKey("token")

    fun getName(): Flow<String?> = dataStore.data.map {
        it[nameKey]
    }

    fun getUserId(): Flow<String?> = dataStore.data.map {
        it[userIdKey]
    }

    fun getToken(): Flow<String?> = dataStore.data.map {
        it[tokenKey]
    }

    suspend fun setName(name: String) = dataStore.edit {
        it[nameKey] = name
    }

    suspend fun setUserId(userId: String) = dataStore.edit {
        it[userIdKey] = userId
    }

    suspend fun setToken(token: String) = dataStore.edit {
        it[tokenKey] = token
    }

    suspend fun clearPreferences() = dataStore.edit {
        it.clear()
    }

    companion object {
        const val PREFERENCE_NAME = "story_preference"

        @Volatile
        private var instance: AppPreference? = null

        @JvmStatic
        fun getInstance(dataStore: DataStore<Preferences>): AppPreference {
            if (instance == null) {
                synchronized(this) {
                    instance = AppPreference(dataStore)
                }
            }

            return instance!!
        }
    }
}