package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun likeById(id: Long): Post
    suspend fun unlikeById(id: Long): Post
    suspend fun shareById(id: Long)
    suspend fun viewsById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post): Post

    suspend fun insertLocal(post: PostEntity)
    suspend fun saveRemote(post: Post): Post

    suspend fun getAllASync()
}