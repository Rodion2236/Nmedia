package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardviewPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AppConfig
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
    fun onRetrySend(post: Post)
    fun onImageClick(url: String)
}

class PostAdapter(
    private val onPostInteractionListener: OnPostInteractionListener
): PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostViewHolder.PostDiffCallback) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.cardview_post
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.cardview_post -> {
                val binding = CardviewPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onPostInteractionListener)
            }
            R.layout.card_ad -> {
                val binding = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            else -> error("unknown item type: $viewType")
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("unknown item type")
        }
    }
}

class AdViewHolder(private val binding: CardAdBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        Glide.with(itemView)
            .load("${AppConfig.IMAGE_BASE_URL}${ad.image}")
            .placeholder(R.drawable.ic_load_24)
            .error(R.drawable.ic_broken_24)
            .into(binding.image)
    }
}

class PostViewHolder(
    private val binding: CardviewPostBinding,
    private val onPostInteractionListener: OnPostInteractionListener
): RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            binding.syncStatus.isVisible = !post.sent
            binding.syncStatus.setImageResource(
                if (!post.sent) R.drawable.ic_progress_sync_24 else R.drawable.ic_check
            )
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

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    menu.findItem(R.id.retry)?.isVisible = !post.sent

                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onPostInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onPostInteractionListener.onEdit(post)
                                true
                            }
                            R.id.retry -> {
                                onPostInteractionListener.onRetrySend(post)
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
                .load("${AppConfig.AVATAR_BASE_URL}${post.authorAvatar}")
                .placeholder(R.drawable.ic_load_24)
                .error(R.drawable.ic_broken_24)
                .circleCrop()
                .timeout(10_000)
                .into(binding.avatar)

            val isImage = when (post.attachment?.type) {
                AttachmentType.IMAGE -> true
                else -> false
            }

            if (isImage) {
                binding.attachmentImage.visibility = View.VISIBLE
                Glide.with(itemView)
                    .load("${AppConfig.IMAGE_BASE_URL}${post.attachment?.url}")
                    .placeholder(R.drawable.ic_load_24)
                    .error(R.drawable.ic_broken_24)
                    .timeout(10_000)
                    .into(binding.attachmentImage)

                binding.attachmentImage.setOnClickListener {
                    post.attachment?.url?.let { url ->
                        onPostInteractionListener.onImageClick(url)
                    }
                }
            } else {
                binding.attachmentImage.visibility = View.GONE
                binding.attachmentImage.setOnClickListener(null)
            }
        }
    }

    object PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(
            oldItem: FeedItem,
            newItem: FeedItem
        ): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: FeedItem,
            newItem: FeedItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}