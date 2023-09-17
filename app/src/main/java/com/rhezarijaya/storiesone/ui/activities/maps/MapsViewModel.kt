package com.rhezarijaya.storiesone.ui.activities.maps

import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import com.rhezarijaya.storiesone.data.StoryRepository
import com.rhezarijaya.storiesone.data.network.LocationType

@ExperimentalPagingApi
class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun getStories() = storyRepository.getStories(location = LocationType.LOCATION_ON)
}