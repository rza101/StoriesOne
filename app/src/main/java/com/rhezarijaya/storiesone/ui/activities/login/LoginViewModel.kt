package com.rhezarijaya.storiesone.ui.activities.login

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesone.data.UserRepository
import com.rhezarijaya.storiesone.data.network.response.LoginResult

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    fun login(email: String, password: String) = userRepository.login(email, password)

    suspend fun saveLoginData(loginResult: LoginResult) = userRepository.saveLoginData(loginResult)
}