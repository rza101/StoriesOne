package com.rhezarijaya.storiesone.ui.activities.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.databinding.ActivityLoginBinding
import com.rhezarijaya.storiesone.ui.activities.main.MainActivity
import com.rhezarijaya.storiesone.ui.activities.register.RegisterActivity
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.Result
import com.rhezarijaya.storiesone.util.ViewModelFactory
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInputsEnabled(true)
        setLoginButtonEnabled()

        binding.edLoginEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setLoginButtonEnabled()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edLoginPassword.addTextChangedListener(
            onTextChanged = { _, _, _, _ ->
                setLoginButtonEnabled()
            }
        )

        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (email.isNotEmpty() && Helpers.isEmailValid(email) &&
                password.isNotEmpty() && Helpers.isPasswordValid(password)
            ) {
                setInputsEnabled(false)

                loginViewModel.login(email, password).observe(this) { result ->
                    when (result) {
                        is Result.Success -> {
                            setLoadingVisible(false)

                            Toast.makeText(
                                this,
                                getString(R.string.login_success), Toast.LENGTH_SHORT
                            ).show()

                            lifecycleScope.launch {
                                loginViewModel.saveLoginData(result.data.loginResult)

                                // terdapat bug dimana terkadang setelah login terdapat masalah bad header
                                // hasil analisis saya dikarenakan object api service pada injection masih
                                // belum menggunakan bearer token dari login karena merupakan singleton
                                // sehingga jika tidak di clear instancenya (atau restart aplikasi)
                                // akan masih menggunakan api service yang tanpa bearer token
                                ViewModelFactory.clearInstance()

                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        }

                        is Result.Loading -> {
                            setLoadingVisible(true)
                        }

                        is Result.Error -> {
                            result.exception.getData()?.let { exception ->
                                setLoadingVisible(false)
                                setInputsEnabled(true)
                                Helpers.retrofitExceptionHandler(
                                    this,
                                    exception
                                )
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.invalid_inputs), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setInputsEnabled(isEnabled: Boolean) = binding.run {
        btnLogin.isEnabled = isEnabled
        btnRegister.isEnabled = isEnabled
        edLoginEmail.isEnabled = isEnabled
        edLoginPassword.isEnabled = isEnabled
    }

    private fun setLoadingVisible(isVisible: Boolean) {
        binding.progressBar.isVisible = isVisible
    }

    private fun setLoginButtonEnabled() = binding.run {
        val email = edLoginEmail.text.toString()
        val password = edLoginPassword.text.toString()

        btnLogin.isEnabled =
            email.isNotEmpty() && Helpers.isEmailValid(email) &&
                    password.isNotEmpty() && Helpers.isPasswordValid(password)
    }
}