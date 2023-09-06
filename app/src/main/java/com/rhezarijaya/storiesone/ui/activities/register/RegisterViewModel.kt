package com.rhezarijaya.storiesone.ui.activities.register

import androidx.lifecycle.ViewModel
import com.rhezarijaya.storiesone.data.UserRepository

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
    fun register(name: String, email: String, password: String) =
        userRepository.register(name, email, password)
}