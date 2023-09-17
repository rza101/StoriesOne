package com.rhezarijaya.storiesone.util

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
    timeMillis: Long = 3000,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null

    val countDownLatch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            countDownLatch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    observeForever(observer)

    try {
        afterObserve()

        if (!countDownLatch.await(timeMillis, TimeUnit.MILLISECONDS)) {
            throw TimeoutException("Livedata value was never set")
        }
    } finally {
        removeObserver(observer)
    }

    return data!!
}