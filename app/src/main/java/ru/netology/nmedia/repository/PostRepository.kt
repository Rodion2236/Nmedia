package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun likeById(id: Long, callback: ActionCallback)
    fun unlikeById(id: Long, callback: ActionCallback)
    fun shareById(id: Long, callback: ActionCallback)
    fun viewsById(id: Long, callback: ActionCallback)
    fun removeById(id: Long, callback: ActionCallback)
    fun save(post: Post, callback: SaveCallback)

    fun getAllASync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
    }

    interface ActionCallback {
        fun onSuccess() {}
        fun onError(e: Exception) {}
    }

    interface SaveCallback {
        fun onSuccess(post: Post) {}
        fun onError(e: Exception) {}
    }
}