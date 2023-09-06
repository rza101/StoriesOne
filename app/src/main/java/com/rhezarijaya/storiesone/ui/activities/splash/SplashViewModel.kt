package com.rhezarijaya.storiesone.ui.activities.splash

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesone.data.UserRepository

class SplashViewModel(private val userRepository: UserRepository) : ViewModel() {
    suspend fun isLoggedIn(): Boolean = userRepository.getLoginData() != null
}