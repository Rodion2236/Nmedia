package ru.netology.nmedia.repository

import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post
import java.util.Date
import java.util.Locale

class PostRepositoryFilesImpl(private val context: Context): PostRepository {

    private var posts = listOf<Post>()
        set(value) {
            field = value
            sync()
        }
    private val _data = MutableLiveData(posts)
    override fun get(): LiveData<List<Post>> = _data

    init {
        val file = context.filesDir.resolve(FILENAME)
        if (file.exists()) {
            context.openFileInput(FILENAME).bufferedReader().use {
                posts = gson.fromJson(it, type)
                nextId = posts.maxOfOrNull { it.id }?.inc() ?: 1
                _data.value = posts
            }
        }
    }

    private fun sync() {
        context.openFileOutput(FILENAME, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }

    private var nextId = 1L

    override fun likeById(id: Long) {

        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(
                    likedByMe = !post.likedByMe,
                    likes = if (post.likedByMe) post.likes - 1 else post.likes + 1
                )
            } else {
                post
            }
        }
        _data.value = posts
    }

    override fun shareById(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(
                    shares = post.shares + 1
                )
            } else {
                post
            }
        }
        _data.value = posts
    }

    override fun viewsById(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                post.copy(
                    views = post.views + 1,
                    viewed = true
                )
            } else {
                post
            }
        }
        _data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        _data.value = posts
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(post.copy(
                nextId++,
                "Me",
                published = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )
            ) + posts
        } else {
            posts.map {
                if (it.id != post.id) it else it.copy(content = post.content)
            }
        }
        _data.value = posts
    }

    companion object {
        private const val FILENAME = "posts.json"

        private val gson = Gson()
        private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    }
}