package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardviewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryNetwork
import ru.netology.nmedia.util.Utils
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
            binding.published.text = Utils.formatPublished(post.published)
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

            if (!post.video.isNullOrBlank()) {
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

            Glide.with(itemView)
                .load("${PostRepositoryNetwork.AVATAR_BASE_URL}${post.authorAvatar}")
                .placeholder(R.drawable.ic_load_24)
                .error(R.drawable.ic_broken_24)
                .circleCrop()
                .timeout(10_000)
                .into(binding.avatar)

            if (post.attachment != null && post.attachment.type == "IMAGE") {
                binding.attachmentImage.visibility = View.VISIBLE
                Glide.with(itemView)
                    .load("${PostRepositoryNetwork.IMAGE_BASE_URL}${post.attachment.url}")
                    .placeholder(R.drawable.ic_load_24)
                    .error(R.drawable.ic_broken_24  )
                    .timeout(10_000)
                    .into(binding.attachmentImage)
            } else {
                binding.attachmentImage.visibility = View.GONE
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