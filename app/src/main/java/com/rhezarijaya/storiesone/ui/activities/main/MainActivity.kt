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
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.map
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.databinding.ActivityMainBinding
import com.rhezarijaya.storiesone.ui.activities.create.CreateActivity
import com.rhezarijaya.storiesone.ui.activities.detail.DetailActivity
import com.rhezarijaya.storiesone.ui.activities.login.LoginActivity
import com.rhezarijaya.storiesone.ui.activities.maps.MapsActivity
import com.rhezarijaya.storiesone.ui.adapters.LoadingStateAdapter
import com.rhezarijaya.storiesone.ui.adapters.StoryItemAdapter
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.ViewModelFactory
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyItemAdapter: StoryItemAdapter

    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val intentCreateLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == CREATE_POST_RESULT_CODE) {
                storyItemAdapter.refresh() // untuk mengambil ulang data, saya menggunakan refresh dari adapter
                loadAdapter() // adapter juga perlu di load ulang agar post terbaru muncul di atas
            }
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

            binding.rvStories.layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    LinearLayoutManager(this@MainActivity)
                } else {
                    GridLayoutManager(this@MainActivity, 2)
                }

            binding.fabAdd.setOnClickListener {
                intentCreateLauncher.launch(Intent(this@MainActivity, CreateActivity::class.java))
            }

            loadAdapter()

            mainViewModel.stories.observe(this@MainActivity) { data ->
                storyItemAdapter.submitData(lifecycle, data.map {
                    Helpers.storyEntitytoStoryResponse(it)
                })
            }
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

    private fun loadAdapter() {
        storyItemAdapter = StoryItemAdapter { story, binding ->
            val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair(binding.ivItemPhoto, "detailPhoto"),
                Pair(binding.tvItemName, "name"),
            )
            startActivity(Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.STORY_ITEM_KEY, story)
            }, optionsCompat.toBundle())
        }

        binding.rvStories.adapter = storyItemAdapter.withLoadStateFooter(LoadingStateAdapter {
            storyItemAdapter.retry()
        })

        binding.rvStories.smoothScrollToPosition(0)

        storyItemAdapter.addLoadStateListener {
            // menampilkan loading hanya saat state refresh, bukan append atau prepend
            // dan ketika state refresh bernilai loading
            // sehingga saat halaman masih kosong atau refresh, maka akan tampil loading
            setLoadingVisible(it.refresh == LoadState.Loading)
        }
    }

    private fun setLoadingVisible(isVisible: Boolean) {
        binding.progressBar.isVisible = isVisible
    }

    companion object {
        const val CREATE_POST_RESULT_CODE = 100
    }
}