package ru.netology.nmedia.dto

data class PostEntity(
    val id: Long = 0,
    val author: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val video: String? = null,
    val viewed: Boolean = false
)