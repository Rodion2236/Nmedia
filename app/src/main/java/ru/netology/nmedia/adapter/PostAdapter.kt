package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardviewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.formatCount

typealias OnItemLikeListener = (Long) -> Unit
typealias OnItemShareListener = (Long) -> Unit
typealias OnItemViewListener = (Long) -> Unit

class PostAdapter(
    private val onItemLikeListener: OnItemLikeListener,
    private val onItemShareListener: OnItemShareListener,
    private val onItemViewListener: OnItemViewListener
): ListAdapter<Post, PostViewHolder>(PostViewHolder.PostDiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {
        val binding = CardviewPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onItemLikeListener, onItemShareListener, onItemViewListener)
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
    private val onItemLikeListener: OnItemLikeListener,
    private val onItemShareListener: OnItemShareListener,
    private val onItemViewListener: OnItemViewListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content

            like.setImageResource(if (post.likedByMe) R.drawable.ic_like else R.drawable.ic_empty_like)

            like.setOnClickListener {
                onItemLikeListener(post.id)
            }

            share.setOnClickListener {
                onItemShareListener(post.id)
            }

            if (!post.viewed) {
                onItemViewListener(post.id)
            }

                likeTv.text = formatCount(post.likes)
                shareTv.text = formatCount(post.shares)
                viewsTv.text = formatCount(post.views)
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



