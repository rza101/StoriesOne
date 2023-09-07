package com.rhezarijaya.storiesone.ui.activities.create

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesone.data.StoryRepository
import com.rhezarijaya.storiesone.util.Helpers
import java.io.File

class CreateViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    var cameraImageFilepath: String? = null
    private var imageFile: File? = null

    fun addStory(description: String, photo: File, latitude: Double?, longitude: Double?) =
        storyRepository.addStory(description, photo, latitude, longitude)

    fun getImageFile() = imageFile

    fun setImageFile(file: File) {
        Helpers.adjustImageRotation(file)
        imageFile = file
    }
}