package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import okio.IOException
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.database.PostDatabase
import ru.netology.nmedia.dto.PostEntity
import ru.netology.nmedia.dto.PostRemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: PostApi,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: PostDatabase,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    val afterKey = postRemoteKeyDao.max()
                    if (afterKey == null) {
                        apiService.getLatest(state.config.initialLoadSize)
                    } else {
                        apiService.getAfter(afterKey, state.config.pageSize)
                    }
                }

                LoadType.APPEND -> {
                    val beforeKey = postRemoteKeyDao.min() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                    apiService.getBefore(beforeKey, state.config.pageSize)
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val body = response.body() ?: emptyList()

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (body.isNotEmpty()) {
                            postDao.insert(body.map { PostEntity.fromDto(it) })

                            postRemoteKeyDao.insert(PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                key = body.first().id
                            ))
                            postRemoteKeyDao.insert(PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                key = body.last().id
                            ))
                        }
                    }

                    LoadType.APPEND -> {
                        if (body.isNotEmpty()) {
                            postDao.insert(body.map { PostEntity.fromDto(it) })

                            postRemoteKeyDao.insert(PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                key = body.last().id
                            ))
                        }
                    }
                    LoadType.PREPEND -> {}
                }
            }

            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())

        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}