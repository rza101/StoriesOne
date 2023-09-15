package com.rhezarijaya.storiesone.ui.activities.detail

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.data.network.response.Story
import com.rhezarijaya.storiesone.databinding.ActivityDetailBinding
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.loadImage

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        val story = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(STORY_ITEM_KEY, Story::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(STORY_ITEM_KEY)
        }

        if (story == null) {
            finish()
        } else {
            binding.apply {
                ivDetailPhoto.loadImage(story.photoUrl)

                tvDetailName.text = story.name
                tvDetailDescription.text = story.description
                tvDetailCreatedAt.text =
                    getString(R.string.created_at_format, Helpers.apiDateFormatter(story.createdAt))

                if (story.lat != null && story.lon != null) {
                    tvDetailCoordinate.text =
                        getString(R.string.coordinate_format, story.lat, story.lon)
                } else {
                    tvDetailCoordinate.isVisible = false
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val STORY_ITEM_KEY = "STORY_ITEM_KEY"
    }
}