package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.database.PostDatabase
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryRoomImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val empty = Post(
        0,
        "",
        "",
        "",
        0,
        false
    )
    private val database = PostDatabase.getInstance(application)
    private val repository: PostRepository = PostRepositoryRoomImpl(database.postDao())

    val data = repository.get()
    val edited = MutableLiveData(empty)
    fun like(id: Long) = repository.likeById(id)
    fun share(id: Long) = repository.shareById(id)
    fun views(id: Long) = repository.viewsById(id)
    fun remove(id: Long) = repository.removeById(id)

    fun save(content: String, postId: Long = 0L) {
        if (content.isBlank()) return
        val newPost = if (postId == 0L) {
            Post(
                id = 0,
                "Родион Косолапкин",
                content = content,
                SimpleDateFormat("dd MMMM HH:mm", Locale("ru")).format(Date()),
                likedByMe = false,
                video = null
            )
        } else {
            Post(
                id = postId,
                "Родион Косолапкин",
                content = content,
                SimpleDateFormat("dd MMMM HH:mm", Locale("ru")).format(Date()),
                likedByMe = false,
                video = null
            )
        }
        repository.save(newPost)
        if (postId != 0L) {
            edited.value = empty
        }
    }
}