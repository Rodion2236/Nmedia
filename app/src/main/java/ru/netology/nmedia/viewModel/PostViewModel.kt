package ru.netology.nmedia.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl

class PostViewModel: ViewModel() {

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
    fun save (text: String) {
        edited.value?.let {
            val content = text.trim()
            if (content.isNotEmpty() && content != it.content) {
                repository.save(it.copy(content = content))
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = empty
    }
}