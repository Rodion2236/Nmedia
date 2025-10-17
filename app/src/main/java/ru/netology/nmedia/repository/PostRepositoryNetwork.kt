package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryNetwork() : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/"
        val jsonType = "application/json".toMediaType()
    }

    override fun getAllASync(callback: PostRepository.GetAllCallback) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body.string()
                    try {
                        callback.onSuccess(gson.fromJson(body, typeToken.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    private fun makeActionCall(request: Request, callback: PostRepository.ActionCallback) {
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess()
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    override fun likeById(id: Long, callback: PostRepository.ActionCallback) {
        val request = Request.Builder()
            .post(gson.toJson(JsonObject()).toRequestBody(jsonType))
            .url("${BASE_URL}api/slow/posts/$id/likes")
            .build()

        makeActionCall(request, callback)
    }

    override fun unlikeById(id: Long, callback: PostRepository.ActionCallback) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}api/slow/posts/$id/likes")
            .build()

        makeActionCall(request, callback)
    }

    override fun shareById(id: Long, callback: PostRepository.ActionCallback) {
        val request = Request.Builder()
            .post(gson.toJson(JsonObject()).toRequestBody(jsonType))
            .url("${BASE_URL}api/slow/posts/$id/shares")
            .build()

        makeActionCall(request, callback)
    }

    override fun viewsById(id: Long, callback: PostRepository.ActionCallback) {
        val request = Request.Builder()
            .post(gson.toJson(JsonObject()).toRequestBody(jsonType))
            .url("${BASE_URL}api/slow/posts/$id/views")
            .build()

        makeActionCall(request, callback)
    }

    override fun removeById(id: Long, callback: PostRepository.ActionCallback) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}api/slow/posts/$id")
            .build()

        makeActionCall(request, callback)
    }

    override fun save(post: Post, callback: PostRepository.SaveCallback) {
        val request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}api/slow/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body.string()
                try {
                    val savedPost = gson.fromJson(body, Post::class.java)
                    callback.onSuccess(savedPost)
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }
}