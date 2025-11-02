package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val onErrorRetry: (() -> Unit)? = null
)

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)