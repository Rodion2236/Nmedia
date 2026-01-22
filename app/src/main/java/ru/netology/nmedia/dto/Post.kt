package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}
data class Post(
    override val id: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorId: Long,
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val viewed: Boolean = false,
    val views: Int = 0,
    val video: String? = null,
    val attachment: Attachment? = null,

    val ownedByMe: Boolean = false,
    val sent: Boolean = true,
    val showInFeed: Boolean = true
): FeedItem

data class Ad(
    override val id: Long,
    val image: String,
): FeedItem

data class Attachment(
    val url: String,
    val description: String? = null,
    val type: AttachmentType
)