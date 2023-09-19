package com.rhezarijaya.storiesone.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.rhezarijaya.storiesone.R
import java.io.File

fun ImageView.loadImage(file: File) =
    Glide.with(this.context)
        .load(file)
        .placeholder(R.drawable.baseline_broken_image_24)
        .error(R.drawable.baseline_broken_image_24)
        .into(this)

fun ImageView.loadImage(urlString: String) =
    Glide.with(this.context)
        .load(urlString)
        .placeholder(R.drawable.baseline_broken_image_24)
        .error(R.drawable.baseline_broken_image_24)
        .into(this)