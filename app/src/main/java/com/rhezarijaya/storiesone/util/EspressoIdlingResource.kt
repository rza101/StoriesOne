package com.rhezarijaya.storiesone.util

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingResource {
    private const val IDLING_RESOURCE_NAME = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(IDLING_RESOURCE_NAME)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
    EspressoIdlingResource.increment()
    return try {
        function()
    } finally {
        EspressoIdlingResource.decrement()
    }
}