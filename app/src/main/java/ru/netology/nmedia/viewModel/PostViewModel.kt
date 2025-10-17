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
        _data.postValue(FeedModel(loading = true))
        repository.getAllASync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun like(id: Long) {
        val currentPost = _data.value?.posts?.find { it.id == id } ?: return

        val callback = object : PostRepository.ActionCallback {
            override fun onSuccess() {
                load()
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        }

        if (currentPost.likedByMe) {
            repository.unlikeById(id, callback)
        } else {
            repository.likeById(id, callback)
        }
    }

    fun share(id: Long) {
        repository.shareById(id, object : PostRepository.ActionCallback {
            override fun onSuccess() {
                load()
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun views(id: Long) {
        repository.viewsById(id, object : PostRepository.ActionCallback {
            override fun onSuccess() {}

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun remove(id: Long) {
        repository.removeById(id, object : PostRepository.ActionCallback {
            override fun onSuccess() {
                load()
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save(content: String) {
        edited.value?.let { postToEdit ->
            val trimmed = content.trim()
            if (trimmed.isNotBlank() && trimmed != postToEdit.content) {
                val postToSave = postToEdit.copy(content = trimmed)
                repository.save(postToSave, object : PostRepository.SaveCallback {
                    override fun onSuccess(savedPost: Post) {
                        _postCreated.postValue(Unit)
                        edited.postValue(empty)
                    }

                    override fun onError(e: Exception) {
                        _data.postValue(FeedModel(error = true))
                    }
                })
            }
        }
    }

    fun editPost(id: Long) {
        val postToEdit = _data.value?.posts?.find { it.id == id }
        if (postToEdit != null) {
            edited.value = postToEdit
        }
    }
}