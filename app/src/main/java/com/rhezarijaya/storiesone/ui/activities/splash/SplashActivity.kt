package com.rhezarijaya.storiesone.ui.activities.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import com.rhezarijaya.storiesone.databinding.ActivitySplashBinding
import com.rhezarijaya.storiesone.ui.activities.login.LoginActivity
import com.rhezarijaya.storiesone.ui.activities.main.MainActivity
import com.rhezarijaya.storiesone.util.Constants
import com.rhezarijaya.storiesone.util.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalPagingApi
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val splashViewModel by viewModels<SplashViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ivLogoAnimator =
            ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 1f).setDuration(500)
        val tvAppNameAnimator =
            ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 1f).setDuration(500)

        val animatorSet = AnimatorSet().apply {
            play(ivLogoAnimator).before(tvAppNameAnimator)
            startDelay = 500
        }
        animatorSet.doOnEnd {
            lifecycleScope.launch {
                val isLoggedIn = splashViewModel.isLoggedIn()

                delay(Constants.SPLASH_SCREEN_DELAY)

                if (isLoggedIn) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }

                finish()
            }
        }
        animatorSet.start()
    }
}