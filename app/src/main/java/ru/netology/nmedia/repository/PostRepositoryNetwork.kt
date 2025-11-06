package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity

class PostRepositoryNetwork(private val dao: PostDao) : PostRepository {

    override val data: LiveData<List<Post>> = dao.getAll().map {
        it.map(PostEntity::toDto)
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
            dao.unlikeById(id)
            PostApi.retrofitService.dislikeById(id)
        } catch (e: Exception) {
            dao.unlikeById(id)
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