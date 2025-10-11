package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetwork
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val empty = Post(
        0,
        "",
        "",
        "",
        0,
        false
    )
    private val repository: PostRepository = PostRepositoryNetwork()
    private val _data: MutableLiveData<FeedModel> = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        load()
    }

    fun load() {
        thread {
            _data.postValue((_data.value ?: FeedModel()).copy(loading = true))
            try {
                val posts = repository.get()
                _data.postValue((_data.value ?: FeedModel()).copy(
                    posts = posts,
                    empty = posts.isEmpty(),
                    loading = false
                ))
            } catch (_: Exception) {
                _data.postValue((_data.value ?: FeedModel()).copy(
                    loading = false,
                    error = true
                ))
            }
        }
    }

    fun like(id: Long) {
        thread {
            try {
                val currentPost = _data.value?.posts?.find { it.id == id }
                val updatedPost = if (currentPost?.likedByMe == true) {
                    repository.unlikeById(id)
                } else {
                    repository.likeById(id)
                }

                val currentPosts = _data.value?.posts.orEmpty().toMutableList()
                val index = currentPosts.indexOfFirst { it.id == id }
                if (index != -1) {
                    currentPosts[index] = updatedPost
                }
                _data.postValue(FeedModel(posts = currentPosts, empty = currentPosts.isEmpty()))
            } catch (_: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }
    }

    fun share(id: Long) {
        thread {
            try {
                repository.shareById(id)
                load()
            } catch (_: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }
    }

    fun views(id: Long) {
        thread {
            try {
                repository.viewsById(id)
            } catch (_: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }
    }

    fun remove(id: Long) {
        thread {
            try {
                repository.removeById(id)
                load()
            } catch (_: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }
    }

    fun save(content: String) {
        thread {
            edited.value?.let {
                val text = content.trim()
                if (it.content != text) {
                    repository.save(it.copy(content = text))
                    _postCreated.postValue(Unit)
                }
            }
            edited.postValue(empty)
        }
    }

    fun editPost(id: Long) {
        val postToEdit = _data.value?.posts?.find { it.id == id }
        if (postToEdit != null) {
            edited.value = postToEdit
        }
    }
}