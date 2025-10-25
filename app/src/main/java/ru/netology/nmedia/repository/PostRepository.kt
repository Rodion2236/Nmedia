package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun likeById(id: Long, callback: ActionCallback)
    fun unlikeById(id: Long, callback: ActionCallback)
    fun shareById(id: Long, callback: SimpleActionCallback)
    fun viewsById(id: Long, callback: SimpleActionCallback)
    fun removeById(id: Long, callback: SimpleActionCallback)
    fun save(post: Post, callback: SaveCallback)

    fun getAllASync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Throwable) {}
    }

    interface ActionCallback {
        fun onSuccess(post: Post) {}
        fun onError(e: Throwable) {}
    }

    interface SimpleActionCallback {
        fun onSuccess() {}
        fun onError(e: Throwable) {}
    }

    interface SaveCallback {
        fun onSuccess(post: Post) {}
        fun onError(e: Throwable) {}
    }
}