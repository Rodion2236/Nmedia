package ru.netology.nmedia.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardviewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.formatCount

interface OnPostInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onView(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onVideoClick(post: Post)
    fun onPostClick(post: Post)
}

class PostAdapter(
    private val onPostInteractionListener: OnPostInteractionListener
): ListAdapter<Post, PostViewHolder>(PostViewHolder.PostDiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val binding = CardviewPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onPostInteractionListener)
    }

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int
    ) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardviewPostBinding,
    private val onPostInteractionListener: OnPostInteractionListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content

            like.isChecked = post.likedByMe
            like.text = formatCount(post.likes)

            share.text = formatCount(post.shares)

            like.setOnClickListener {
                onPostInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onPostInteractionListener.onShare(post)
            }

            if (!post.viewed) {
                onPostInteractionListener.onView(post)
            }

            viewsTv.text = formatCount(post.views)

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { item ->
                        when(item.itemId) {
                            R.id.remove -> {
                                onPostInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onPostInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            if (post.video != null) {
                video.visibility = View.VISIBLE

                video.setOnClickListener {
                    onPostInteractionListener.onVideoClick(post)
                }
            } else {
                video.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onPostInteractionListener.onPostClick(post)
            }
        }
    }

    object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean {
            return oldItem == newItem
        }
    }
}