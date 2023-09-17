package com.rhezarijaya.storiesone.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.rhezarijaya.storiesone.data.mediator.StoryRemoteMediator
import com.rhezarijaya.storiesone.data.network.LocationType
import com.rhezarijaya.storiesone.data.network.service.StoryAPIService
import com.rhezarijaya.storiesone.data.room.StoriesDatabase
import com.rhezarijaya.storiesone.data.room.entity.StoryEntity
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.Result
import com.rhezarijaya.storiesone.util.SingleEvent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@ExperimentalPagingApi
class StoryRepository(
    private val storiesDatabase: StoriesDatabase,
    private val storyApiService: StoryAPIService
) {
    fun addStory(description: String, photo: File, latitude: Double?, longitude: Double?) =
        liveData {
            try {
                emit(Result.Loading)
                emit(
                    Result.Success(
                        storyApiService.addStory(
                            description.toRequestBody("text/plain".toMediaType()),
                            MultipartBody.Part.createFormData(
                                "photo",
                                photo.name,
                                Helpers.compressImage(photo)
                                    .asRequestBody("image/jpeg".toMediaType())
                            ),
                            latitude?.toString()?.toRequestBody("text/plain".toMediaType()),
                            longitude?.toString()?.toRequestBody("text/plain".toMediaType()),
                        )
                    )
                )
            } catch (e: Exception) {
                emit(Result.Error(SingleEvent(e)))
            }
        }

    fun getStories(page: Int? = null, size: Int? = null, location: LocationType) = liveData {
        try {
            emit(Result.Loading)
            emit(Result.Success(storyApiService.getStories(page, size, location.type)))
        } catch (e: Exception) {
            emit(Result.Error(SingleEvent(e)))
        }
    }

    fun getStoriesPaged(): LiveData<PagingData<StoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 3,
                initialLoadSize = 3
            ),
            remoteMediator = StoryRemoteMediator(storiesDatabase, storyApiService),
            pagingSourceFactory = {
                storiesDatabase.getStoriesDao().getAllStories()
            }
        ).liveData
    }
}