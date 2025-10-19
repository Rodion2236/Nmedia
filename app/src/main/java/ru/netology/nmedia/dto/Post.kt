package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val viewed: Boolean = false,
    val views: Int = 0,
    val video: String? = null,
    val attachment: Attachment? = null
)

data class Attachment(
    val url: String,
    val description: String,
    val type: String
)