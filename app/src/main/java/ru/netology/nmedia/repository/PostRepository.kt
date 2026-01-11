package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>
    suspend fun getById(id: Long): Post
    fun getNewer(id: Long): Flow<Int>
    suspend fun showAllNewPosts()
    suspend fun likeById(id: Long): Post
    suspend fun unlikeById(id: Long): Post
    suspend fun shareById(id: Long)
    suspend fun viewsById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post, image: File?): Post

    suspend fun insertLocal(post: PostEntity)
    suspend fun saveRemote(post: Post): Post
}