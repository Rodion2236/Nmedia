package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post

class PostRepositoryNetwork : PostRepository {

    private val apiService = PostApi.retrofitService

    override fun getAllASync(callback: PostRepository.GetAllCallback) {
        apiService.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body() ?: emptyList())
                } else {
                    callback.onError(RuntimeException("HTTP ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<List<Post>>, throwable: Throwable) {
                callback.onError(throwable)
            }
        })
    }

    override fun likeById(id: Long, callback: PostRepository.ActionCallback) {
        apiService.likeById(id).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(RuntimeException("HTTP ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Post>, throwable: Throwable) {
                callback.onError(throwable)
            }
        })
    }

    override fun unlikeById(id: Long, callback: PostRepository.ActionCallback) {
        apiService.dislikeById(id).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(RuntimeException("HTTP ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Post>, throwable: Throwable) {
                callback.onError(throwable)
            }
        })
    }

    override fun shareById(id: Long, callback: PostRepository.ActionCallback) {
        callback.onSuccess()
    }

    override fun viewsById(id: Long, callback: PostRepository.ActionCallback) {
        callback.onSuccess()
    }

    override fun removeById(id: Long, callback: PostRepository.ActionCallback) {
        apiService.removeById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(RuntimeException("HTTP ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Unit>, throwable: Throwable) {
                callback.onError(throwable)
            }
        })
    }

    override fun save(post: Post, callback: PostRepository.SaveCallback) {
        apiService.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body()!!)
                } else {
                    callback.onError(RuntimeException("HTTP ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Post>, throwable: Throwable) {
                callback.onError(throwable)
            }
        })
    }
}