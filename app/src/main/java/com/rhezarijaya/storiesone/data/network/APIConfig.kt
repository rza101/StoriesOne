package com.rhezarijaya.storiesone.data.network

import com.rhezarijaya.storiesone.BuildConfig
import com.rhezarijaya.storiesone.data.network.service.StoryAPIService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIConfig {
    fun getStoryAPIService(bearerToken: String): StoryAPIService {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            val requestHeaders = request.newBuilder()
                .addHeader("Authorization", "Bearer $bearerToken")
                .build()
            chain.proceed(requestHeaders)
        }
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            )

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.STORY_API_BASE_URL)
            .client(httpClient)
            .build()

        return retrofit.create(StoryAPIService::class.java)
    }
}