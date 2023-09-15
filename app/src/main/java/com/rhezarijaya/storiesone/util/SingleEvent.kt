package com.rhezarijaya.storiesone.util

class SingleEvent<out T>(private val data: T) {
    @Suppress("MemberVisibilityCanBePrivate")
    var isRetrieved = false
        private set

    fun getData(): T? = if (isRetrieved) {
        null
    } else {
        isRetrieved = true
        data
    }
}