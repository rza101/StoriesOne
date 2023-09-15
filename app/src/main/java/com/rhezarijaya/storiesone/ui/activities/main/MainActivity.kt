package com.rhezarijaya.storiesone.ui.activities.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.databinding.ActivityMainBinding
import com.rhezarijaya.storiesone.ui.activities.create.CreateActivity
import com.rhezarijaya.storiesone.ui.activities.detail.DetailActivity
import com.rhezarijaya.storiesone.ui.activities.login.LoginActivity
import com.rhezarijaya.storiesone.ui.activities.maps.MapsActivity
import com.rhezarijaya.storiesone.ui.adapters.StoryItemAdapter
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.Result
import com.rhezarijaya.storiesone.util.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val intentCreateLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == CREATE_POST_RESULT_CODE) {
                loadData(true)
            }
        }
    private val storyItemAdapter = StoryItemAdapter { story, binding ->
        val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            Pair(binding.ivItemPhoto, "detailPhoto"),
            Pair(binding.tvItemName, "name"),
        )
        startActivity(Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.STORY_ITEM_KEY, story)
        }, optionsCompat.toBundle())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val isLoggedIn = mainViewModel.isLoggedIn()

            if (!isLoggedIn) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.not_logged_in),
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()

                return@launch
            }

            binding.rvStories.apply {
                adapter = storyItemAdapter
                layoutManager =
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        LinearLayoutManager(this@MainActivity)
                    } else {
                        GridLayoutManager(this@MainActivity, 2)
                    }
            }

            binding.fabAdd.setOnClickListener {
                intentCreateLauncher.launch(Intent(this@MainActivity, CreateActivity::class.java))
            }

            binding.fabRefresh.setOnClickListener {
                loadData(true)
            }

            loadData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_change_language -> {
            val alertDialogBuilder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.change_language))
                .setMessage(getString(R.string.change_language_confirm))
                .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

            true
        }

        R.id.action_logout -> {
            val alertDialogBuilder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_confirm))
                .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    lifecycleScope.launch {
                        mainViewModel.logout()
                        ViewModelFactory.clearInstance()
                        dialog.dismiss()

                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.logout_success), Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()

            true
        }

        R.id.action_maps -> {
            startActivity(Intent(this, MapsActivity::class.java))

            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun loadData(scrollToTop: Boolean = false) {
        setInfoText(null)
        mainViewModel.getStories().observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    setLoadingVisible(false)

                    if (!result.data.error) {
                        storyItemAdapter.submitList(result.data.listStory) {
                            // callback ini akan dipanggil setelah selesai melakukan diff
                            // jika perlu scroll ke atas maka akan dilakukan scroll
                            if (scrollToTop) {
                                binding.rvStories.smoothScrollToPosition(0)
                            }
                        }

                        if (result.data.listStory.isEmpty()) {
                            setInfoText(getString(R.string.no_data))
                        }
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.story_list_fetch_failed), Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is Result.Loading -> {
                    setLoadingVisible(true)
                }

                is Result.Error -> {
                    setInfoText(getString(R.string.failed_to_fetch_data))
                    result.exception.getData()?.let { exception ->
                        setLoadingVisible(false)
                        Helpers.retrofitExceptionHandler(
                            this,
                            exception
                        )
                    }
                }
            }
        }
    }

    private fun setInfoText(info: String?) {
        binding.tvMainInfo.text = info ?: ""
        binding.tvMainInfo.isVisible = !info.isNullOrEmpty()
    }

    private fun setLoadingVisible(isVisible: Boolean) {
        binding.progressBar.isVisible = isVisible
        binding.fabRefresh.isEnabled = !isVisible
    }

    companion object {
        const val CREATE_POST_RESULT_CODE = 100
    }
}