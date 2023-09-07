package com.rhezarijaya.storiesone.ui.activities.detail

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.data.network.response.Story
import com.rhezarijaya.storiesone.databinding.ActivityDetailBinding
import com.rhezarijaya.storiesone.util.Helpers

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
                // TODO use extension function
                Glide.with(this@DetailActivity)
                    .load(story.photoUrl)
                    .placeholder(R.drawable.baseline_broken_image_24)
                    .error(R.drawable.baseline_broken_image_24)
                    .into(ivDetailPhoto)
                tvDetailName.text = story.name
                tvDetailDescription.text = story.description
                tvDetailCreatedAt.text =
                    getString(R.string.created_at_format, Helpers.apiDateFormatter(story.createdAt))

                if (story.lat != null && story.lon != null) {
                    tvDetailCoordinate.text =
                        getString(R.string.coordinate_format, story.lat, story.lon)
                } else {
                    tvDetailCoordinate.visibility = View.GONE
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