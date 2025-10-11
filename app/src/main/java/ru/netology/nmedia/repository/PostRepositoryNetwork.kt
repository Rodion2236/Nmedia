package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryNetwork() : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/"
        val jsonType = "application/json".toMediaType()
    }

    override fun get(): List<Post> {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val textBody = response.body.string()

        return gson.fromJson(textBody, type)
    }

    override fun likeById(id: Long): Post {
        val request = Request.Builder()
            .post(gson.toJson(JsonObject()).toRequestBody(jsonType))
            .url("${BASE_URL}api/slow/posts/$id/likes")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val body = response.body.string()
            return gson.fromJson(body, Post::class.java)
        }
    }

    override fun unlikeById(id: Long): Post {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}api/slow/posts/$id/likes")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val body = response.body.string()
            return gson.fromJson(body, Post::class.java)
        }
    }

    override fun shareById(id: Long) {
        val request = Request.Builder()
            .post(gson.toJson(JsonObject()).toRequestBody(jsonType))
            .url("${BASE_URL}api/slow/posts/$id/shares")
            .build()
        client.newCall(request).execute().use { }
    }

    override fun viewsById(id: Long) {
        val request = Request.Builder()
            .post(gson.toJson(JsonObject()).toRequestBody(jsonType))
            .url("${BASE_URL}api/slow/posts/$id/views")
            .build()
        client.newCall(request).execute().use {
        }
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .delete()
            .url("${BASE_URL}api/slow/posts/$id")
            .build()
        client.newCall(request).execute().use {
        }
    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val textBody = response.body.string()

        return gson.fromJson(textBody, Post::class.java)
    }
}