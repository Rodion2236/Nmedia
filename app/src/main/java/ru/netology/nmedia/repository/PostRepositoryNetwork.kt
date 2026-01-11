package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PostRepositoryNetwork @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostApi,
) : PostRepository {

    private val uploadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource(
                apiService
            )
        }
    ).flow

    override suspend fun getById(id: Long): Post {
        return apiService.getById(id)
    }

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = apiService.getBefore(id, 10)
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

    override suspend fun likeById(id: Long): Post {
        return try {
            dao.toggleLike(id)
            apiService.likeById(id)
        } catch (e: Exception) {
            dao.toggleLike(id)
            throw RuntimeException("Like failed", e)
        }
    }

    override suspend fun unlikeById(id: Long): Post {
        return try {
            dao.toggleLike(id)
            apiService.dislikeById(id)
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
            apiService.removeById(id)
        } catch (e: Exception) {
            throw RuntimeException("Remove failed", e)
        }
    }

    override suspend fun save(post: Post, image: File?): Post {
        return suspendCancellableCoroutine { cont ->
            uploadScope.launch {
                try {
                    val media = image?.let { file ->
                        val part = MultipartBody.Part.createFormData(
                            "file",
                            file.name,
                            file.asRequestBody()
                        )
                        apiService.upload(part)
                    }

                    val postToSave = if (media != null) {
                        post.copy(attachment = Attachment(url = media.id, type = AttachmentType.IMAGE))
                    } else {
                        post
                    }

                    val remotePost = apiService.save(postToSave)
                    val entity = PostEntity.fromDto(remotePost)
                    dao.insert(entity)

                    cont.resume(remotePost)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
        }
    }

    override suspend fun insertLocal(post: PostEntity) {
        dao.insert(post)
    }

    override suspend fun saveRemote(post: Post): Post {
        return try {
            apiService.save(post)
        } catch (e: Exception) {
            throw RuntimeException("Save failed(server)", e)
        }
    }
}