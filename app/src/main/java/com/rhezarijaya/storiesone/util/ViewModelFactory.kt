package com.rhezarijaya.storiesone.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rhezarijaya.storiesone.data.StoryRepository
import com.rhezarijaya.storiesone.data.UserRepository
import com.rhezarijaya.storiesone.di.Injection
import com.rhezarijaya.storiesone.ui.activities.create.CreateViewModel
import com.rhezarijaya.storiesone.ui.activities.login.LoginViewModel
import com.rhezarijaya.storiesone.ui.activities.main.MainViewModel
import com.rhezarijaya.storiesone.ui.activities.register.RegisterViewModel
import com.rhezarijaya.storiesone.ui.activities.splash.SplashViewModel

class ViewModelFactory private constructor(
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CreateViewModel::class.java) -> CreateViewModel(
                storyRepository
            ) as T

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(
                userRepository
            ) as T

            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(
                storyRepository, userRepository
            ) as T

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(
                userRepository
            ) as T

            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel(
                userRepository
            ) as T

            else -> throw IllegalArgumentException("Invalid viewmodel ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (instance == null) {
                synchronized(this) {
                    instance = ViewModelFactory(
                        Injection.provideStoryRepository(context),
                        Injection.provideUserRepository(context),
                    )
                }
            }

            return instance!!
        }
    }
}