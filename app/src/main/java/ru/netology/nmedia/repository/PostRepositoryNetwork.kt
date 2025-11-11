package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError

class PostRepositoryNetwork(private val dao: PostDao) : PostRepository {

    override val data = dao.getAll().map { it ->
        it.map {
            it.toDto()
        }
    }.flowOn(Dispatchers.Default)

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostApi.retrofitService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insertBackground(body.map { PostEntity.fromDto(it, showInFeed = false) })

            val hiddenCount = dao.getHiddenPostsCount()
            emit(hiddenCount)
        }
    } .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun showAllNewPosts() {
        dao.showAllNewPosts()
    }

    override suspend fun getAllASync() {
        try {
            val posts = PostApi.retrofitService.getAll()
            dao.insert(posts.map(PostEntity::fromDto))
        } catch (e: Exception) {
            throw RuntimeException("Load failed", e)
        }
    }

    override suspend fun likeById(id: Long): Post {
        return try {
            dao.toggleLike(id)
            PostApi.retrofitService.likeById(id)
        } catch (e: Exception) {
            dao.toggleLike(id)
            throw RuntimeException("Like failed", e)
        }
    }

    override suspend fun unlikeById(id: Long): Post {
        return try {
            dao.toggleLike(id)
            PostApi.retrofitService.dislikeById(id)
        } catch (e: Exception) {
            dao.toggleLike(id)
            throw RuntimeException("Unlike failed", e)
        }
    }

    override suspend fun shareById(id: Long) {
        dao.shareById(id)
    }

    override suspend fun viewsById(id: Long) {
        dao.viewsById(id)
    }

    override suspend fun removeById(id: Long) {
        return try {
            dao.deleteById(id)
            PostApi.retrofitService.removeById(id)
        } catch (e: Exception) {
            throw RuntimeException("Remove failed", e)
        }
    }

    override suspend fun save(post: Post): Post {
        val tempId = PostEntity.tempId()
        val localPost = PostEntity.newLocalPost(post.content)
        dao.insert(localPost)

        return try {
            val remotePost = PostApi.retrofitService.save(post)
            dao.deleteById(tempId)
            val finalPost = PostEntity.fromDto(remotePost)
            dao.insert(finalPost)
            remotePost
        } catch (e: Exception) {
            throw RuntimeException("Save failed", e)
        }
    }

    override suspend fun insertLocal(post: PostEntity) {
        dao.insert(post)
    }

    override suspend fun saveRemote(post: Post): Post {
        return try {
            PostApi.retrofitService.save(post)
        } catch (e: Exception) {
            throw RuntimeException("Save failed(server)", e)
        }
    }
}