package com.rhezarijaya.storiesone.ui.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import com.rhezarijaya.storiesone.data.StoryRepository
import com.rhezarijaya.storiesone.data.UserRepository

@ExperimentalPagingApi
class MainViewModel(
    storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val stories = storyRepository.getStoriesPaged().cachedIn(viewModelScope)

    suspend fun isLoggedIn() = userRepository.getLoginData() != null

    suspend fun logout() = userRepository.logout()
}