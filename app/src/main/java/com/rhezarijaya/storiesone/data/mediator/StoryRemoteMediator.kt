package com.rhezarijaya.storiesone.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.rhezarijaya.storiesone.data.network.LocationType
import com.rhezarijaya.storiesone.data.network.service.StoryAPIService
import com.rhezarijaya.storiesone.data.room.StoriesDatabase
import com.rhezarijaya.storiesone.data.room.entity.RemoteKeys
import com.rhezarijaya.storiesone.data.room.entity.StoryEntity
import com.rhezarijaya.storiesone.util.Helpers

@ExperimentalPagingApi
class StoryRemoteMediator(
    private val storiesDatabase: StoriesDatabase,
    private val storyAPIService: StoryAPIService,
) : RemoteMediator<Int, StoryEntity>() {
    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.PREPEND -> {
                val remoteKey = getFirstItemRemoteKey(state)
                remoteKey?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
            }

            LoadType.REFRESH -> {
                val remoteKey = getClosestItemRemoteKey(state)
                remoteKey?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.APPEND -> {
                val remoteKey = getLastItemRemoteKey(state)
                remoteKey?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
            }
        }

        try {
            val response =
                storyAPIService.getStories(
                    page,
                    state.config.pageSize,
                    LocationType.LOCATION_OFF.type
                )

            val isPaginationEnd = response.listStory.isEmpty()

            val remoteKeysDao = storiesDatabase.getRemoteKeysDao()
            val storiesDao = storiesDatabase.getStoriesDao()

            storiesDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.deleteAllKeys()
                    storiesDao.deleteAllStories()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (isPaginationEnd) null else page + 1

                val remoteKeys = response.listStory.map {
                    RemoteKeys(it.id, prevKey, nextKey)
                }

                remoteKeysDao.insertAllKeys(remoteKeys)
                storiesDao.insertStories(response.listStory.map {
                    Helpers.storyResponseToStoryEntity(it)
                })
            }

            return MediatorResult.Success(endOfPaginationReached = isPaginationEnd)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getClosestItemRemoteKey(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        val anchorPosition = state.anchorPosition
        val closestItem = anchorPosition?.let { state.closestItemToPosition(it) }

        return closestItem?.let {
            storiesDatabase.getRemoteKeysDao().getRemoteKeyById(it.id)
        }
    }

    private suspend fun getFirstItemRemoteKey(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        val firstPage = state.pages.firstOrNull {
            it.data.isNotEmpty()
        }

        return firstPage?.firstOrNull()?.let {
            storiesDatabase.getRemoteKeysDao().getRemoteKeyById(it.id)
        }
    }

    private suspend fun getLastItemRemoteKey(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        val lastPage = state.pages.lastOrNull {
            it.data.isNotEmpty()
        }

        return lastPage?.lastOrNull()?.let {
            storiesDatabase.getRemoteKeysDao().getRemoteKeyById(it.id)
        }
    }

    companion object {
        private const val INITIAL_PAGE_INDEX = 1
    }
}