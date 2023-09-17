package com.rhezarijaya.storiesone.util

import com.rhezarijaya.storiesone.data.room.entity.StoryEntity

object DummyGenerator {
    fun generateDummyStories(): List<StoryEntity> {
        val stories = arrayListOf<StoryEntity>()

        for (i in 1..100) {
            stories.add(
                StoryEntity(
                    id = i.toString(),
                    photoUrl = "",
                    createdAt = "Created at : $i",
                    name = "Name $i",
                    description = "Description $i",
                    lon = null,
                    lat = null
                )
            )
        }

        return stories
    }
}