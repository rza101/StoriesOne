package com.rhezarijaya.storiesone.util

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rhezarijaya.storiesone.data.room.entity.StoryEntity

class StoryPagingSource : PagingSource<Int, LiveData<List<StoryEntity>>>() {
    override fun getRefreshKey(state: PagingState<Int, LiveData<List<StoryEntity>>>) = 0

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<StoryEntity>>> =
        LoadResult.Page(emptyList(), 0, 1)

    companion object {
        fun getSnapshot(storyList: List<StoryEntity>) = PagingData.from(storyList)
    }
}