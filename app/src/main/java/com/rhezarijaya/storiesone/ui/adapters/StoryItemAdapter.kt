package com.rhezarijaya.storiesone.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rhezarijaya.storiesone.data.network.response.Story
import com.rhezarijaya.storiesone.databinding.ItemStoryBinding
import com.rhezarijaya.storiesone.util.loadImage

class StoryItemAdapter(private val onItemClick: (Story, ItemStoryBinding) -> Unit) :
    PagingDataAdapter<Story, StoryItemAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class ViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            val binding = binding

            binding.tvItemName.text = story.name
            binding.ivItemPhoto.loadImage(story.photoUrl)

            itemView.setOnClickListener {
                onItemClick(story, binding)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Story, newItem: Story) =
                oldItem == newItem
        }
    }
}