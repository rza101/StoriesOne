package com.rhezarijaya.storiesone.ui.activities.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.map
import androidx.recyclerview.widget.ListUpdateCallback
import com.rhezarijaya.storiesone.data.StoryRepository
import com.rhezarijaya.storiesone.data.UserRepository
import com.rhezarijaya.storiesone.data.room.entity.StoryEntity
import com.rhezarijaya.storiesone.ui.adapters.StoryItemAdapter
import com.rhezarijaya.storiesone.util.DummyGenerator
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.MainDispatcherRule
import com.rhezarijaya.storiesone.util.StoryPagingSource
import com.rhezarijaya.storiesone.util.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private val updateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
    }

    @Test
    fun `when load story success, data should not null`() = runTest {
        val dummyData = DummyGenerator.generateDummyStories()
        val pagingData = StoryPagingSource.getSnapshot(dummyData)

        val expectedValue = MutableLiveData<PagingData<StoryEntity>>()
        expectedValue.value = pagingData

        `when`(storyRepository.getStoriesPaged()).thenReturn(expectedValue)

        val mainViewModel = MainViewModel(storyRepository, userRepository)

        val actualData = mainViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryItemAdapter.DIFF_CALLBACK,
            updateCallback = updateCallback,
            workerDispatcher = Dispatchers.Main
        )

        differ.submitData(actualData.map {
            Helpers.storyEntitytoStoryResponse(it)
        })

        assertNotNull(differ.snapshot())
        assertEquals(dummyData.size, differ.snapshot().size)
        assertEquals(
            dummyData[0],
            differ.snapshot()[0]?.let { Helpers.storyResponseToStoryEntity(it) }
        )
    }

    @Test
    fun `when load empty story, data count should be zero`() = runTest {
        val dummyData = emptyList<StoryEntity>()
        val pagingData = StoryPagingSource.getSnapshot(dummyData)

        val expectedValue = MutableLiveData<PagingData<StoryEntity>>()
        expectedValue.value = pagingData

        `when`(storyRepository.getStoriesPaged()).thenReturn(expectedValue)

        val mainViewModel = MainViewModel(storyRepository, userRepository)

        val actualData = mainViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryItemAdapter.DIFF_CALLBACK,
            updateCallback = updateCallback,
            workerDispatcher = Dispatchers.Main
        )

        differ.submitData(actualData.map {
            Helpers.storyEntitytoStoryResponse(it)
        })

        assertEquals(0, differ.snapshot().size)
    }
}