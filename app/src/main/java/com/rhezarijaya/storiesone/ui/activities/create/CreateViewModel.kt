package com.rhezarijaya.storiesone.ui.activities.create

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesone.data.StoryRepository
import java.io.File

class CreateViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun addStory(description: String, photo: File, latitude: Double?, longitude: Double?) =
        storyRepository.addStory(description, photo, latitude, longitude)
}