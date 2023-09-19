package com.rhezarijaya.storiesone.ui.activities.register

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.paging.ExperimentalPagingApi
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.databinding.ActivityRegisterBinding
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.Result
import com.rhezarijaya.storiesone.util.ViewModelFactory

@ExperimentalPagingApi
class RegisterActivity : AppCompatActivity() {
    private val registerViewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInputsEnabled(true)
        setRegisterButtonEnabled()

        binding.edRegisterEmail.addTextChangedListener(
            onTextChanged = { _, _, _, _ ->
                setRegisterButtonEnabled()
            }
        )

        binding.edRegisterPassword.addTextChangedListener(
            onTextChanged = { _, _, _, _ ->
                setRegisterButtonEnabled()
            }
        )

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if (name.isNotEmpty() &&
                email.isNotEmpty() && Helpers.isEmailValid(email) &&
                password.isNotEmpty() && Helpers.isPasswordValid(password)
            ) {
                setInputsEnabled(false)

                registerViewModel.register(name, email, password).observe(this) { result ->
                    when (result) {
                        is Result.Success -> {
                            setLoadingVisible(false)

                            Toast.makeText(
                                this,
                                getString(R.string.register_success),
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()
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
    }

    private fun setInputsEnabled(isEnabled: Boolean) = binding.run {
        btnRegister.isEnabled = isEnabled
        edRegisterName.isEnabled = isEnabled
        edRegisterEmail.isEnabled = isEnabled
        edRegisterPassword.isEnabled = isEnabled
    }

    private fun setLoadingVisible(isVisible: Boolean) {
        binding.progressBar.isVisible = isVisible
    }

    private fun setRegisterButtonEnabled() = binding.run {
        val name = edRegisterName.text.toString()
        val email = edRegisterEmail.text.toString()
        val password = edRegisterPassword.text.toString()

        btnRegister.isEnabled =
            name.isNotEmpty() &&
                    email.isNotEmpty() && Helpers.isEmailValid(email) &&
                    password.isNotEmpty() && Helpers.isPasswordValid(password)
    }
}