package ru.netology.nmedia.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import okio.IOException
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post

class PostPagingSource(private val apiService: PostApi) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        return try {
            val result = when (params) {
                is LoadParams.Refresh -> {
                    val key = params.key
                    if (key == null) {
                        apiService.getLatest(params.loadSize)
                    } else {
                        apiService.getBefore(key, params.loadSize)
                    }
                }

                is LoadParams.Append -> {
                    val key = params.key
                    apiService.getAfter(key, params.loadSize)
                }

                is LoadParams.Prepend -> {
                    return LoadResult.Page(
                        data = emptyList(),
                        prevKey = null,
                        nextKey = params.key
                    )
                }
            }

            if (!result.isSuccessful) {
                throw HttpException(result)
            }

            val data = result.body().orEmpty()
            LoadResult.Page(
                data = data,
                prevKey = if (data.isEmpty()) null else data.firstOrNull()?.id,
                nextKey = if (data.isEmpty()) null else data.lastOrNull()?.id
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}