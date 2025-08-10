package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl

class PostViewModel(application: Application): AndroidViewModel(application) {

    private val empty = Post(
        0,
        "",
        "",
        "",
        0,
        false
    )

    private val repository: PostRepository = PostRepositoryInMemoryImpl()
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
                "",
                content = content,
                "",
                likedByMe = false,
            )
        } else {
            Post(
                id = postId,
                "",
                content = content,
                "",
                likedByMe = false,
            )
        }
        repository.save(newPost)

        if (postId != 0L) {
            edited.value = empty
        }
    }
}