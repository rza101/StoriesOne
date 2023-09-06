package com.rhezarijaya.storiesone.data

import androidx.lifecycle.liveData
import com.rhezarijaya.storiesone.data.datastore.AppPreference
import com.rhezarijaya.storiesone.data.network.response.LoginResult
import com.rhezarijaya.storiesone.data.network.service.StoryAPIService
import com.rhezarijaya.storiesone.util.Result
import com.rhezarijaya.storiesone.util.SingleEvent
import kotlinx.coroutines.flow.first

class UserRepository(
    private val storyApiService: StoryAPIService,
    private val appPreference: AppPreference
) {
    suspend fun getLoginData(): LoginResult? {
        appPreference.getName().first()?.let { name ->
            appPreference.getUserId().first()?.let { userId ->
                appPreference.getToken().first()?.let { token ->
                    return LoginResult(name, userId, token)
                }
            }
        }

        return null
    }

    fun login(email: String, password: String) = liveData {
        try {
            emit(Result.Loading)
            emit(Result.Success(storyApiService.login(email, password)))
        } catch (e: Exception) {
            emit(Result.Error(SingleEvent(e)))
        }
    }

    suspend fun logout() {
        appPreference.clearPreferences()
    }

    fun register(name: String, email: String, password: String) = liveData {
        try {
            emit(Result.Loading)
            emit(Result.Success(storyApiService.register(name, email, password)))
        } catch (e: Exception) {
            emit(Result.Error(SingleEvent(e)))
        }
    }

    suspend fun saveLoginData(loginResult: LoginResult) {
        appPreference.setName(loginResult.name)
        appPreference.setUserId(loginResult.userId)
        appPreference.setToken(loginResult.token)
    }
}