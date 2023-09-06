package com.rhezarijaya.storiesone.ui.activities.main

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesone.data.StoryRepository
import com.rhezarijaya.storiesone.data.UserRepository
import com.rhezarijaya.storiesone.data.network.LocationType

class MainViewModel(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    fun getStories() = storyRepository.getStories(null, null, LocationType.LOCATION_OFF)

    suspend fun logout() = userRepository.logout()
}