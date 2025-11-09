package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer(id: Long): Flow<Int>
    suspend fun showAllNewPosts()
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