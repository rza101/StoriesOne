package com.rhezarijaya.storiesone.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.io.InputStreamReader

object JsonConverter {
    fun readStringFromAssets(filename: String): String {
        val applicationContext = ApplicationProvider.getApplicationContext<Context>()

        val inputStream = applicationContext.assets.open(filename)
        val inputStreamReader = InputStreamReader(inputStream, "UTF-8")

        val stringBuilder = StringBuilder()
        inputStreamReader.readLines().forEach {
            stringBuilder.append(it)
        }

        return stringBuilder.toString()
    }
}