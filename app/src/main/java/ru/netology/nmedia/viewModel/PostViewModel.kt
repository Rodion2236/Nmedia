package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.database.PostDatabase
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetwork
import ru.netology.nmedia.util.SingleLiveEvent

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
    private val repository: PostRepository = PostRepositoryNetwork(
        PostDatabase.getInstance(application).postDao()
    )
    val data: LiveData<FeedModel> = repository.data.map {
        FeedModel(
            it,
            it.isEmpty()
        )
    }
    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.getAllASync()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(
                    error = true,
                    onErrorRetry = { load() }
                )
            }
        }
    }

    private val pendingLikeActions = mutableSetOf<Long>()

    fun like(id: Long) = viewModelScope.launch {
        if (pendingLikeActions.contains(id)) return@launch
        pendingLikeActions.add(id)
        try {
            val currentPost = data.value?.posts?.find { it.id == id } ?: return@launch
            if (currentPost.likedByMe) {
                repository.unlikeById(id)
            } else {
                repository.likeById(id)
            }
        } catch (_: Exception) {
            val currentPost = data.value?.posts?.find { it.id == id } ?: return@launch
            val postToBack = currentPost.copy(likedByMe = !currentPost.likedByMe)
            val entity = PostEntity.fromDto(postToBack)
            repository.insertLocal(entity)
            _state.value = _state.value?.copy(
                error = true
            )
        } finally {
            pendingLikeActions.remove(id)
        }
    }

    fun share(id: Long) = viewModelScope.launch {
        try {
            repository.shareById(id)
        } catch (_: Exception) {
            _state.value = _state.value?.copy(
                error = true
            )
        }
    }

    fun views(id: Long) = viewModelScope.launch {
        try {
            repository.viewsById(id)
        } catch (_: Exception) {
            _state.value = _state.value?.copy(
                error = true
            )
        }
    }

    fun remove(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (_: Exception) {
            _state.value = _state.value?.copy(
                error = true
            )
        }
    }

    fun save(content: String) = viewModelScope.launch {
        try {
            edited.value?.let { postToEdit ->
                val trimmed = content.trim()
                if (trimmed.isNotBlank() && trimmed != postToEdit.content) {
                    val postToSave = postToEdit.copy(content = trimmed)
                    repository.save(postToSave)
                    _postCreated.postValue(Unit)
                    edited.postValue(empty)
                }
            }
        } catch (_: Exception) {
            _state.value = _state.value?.copy(
                error = true
            )
        }
    }

    fun retrySend(post: Post) = viewModelScope.launch {
        try {
            val savedPost = repository.saveRemote(post.copy(id = 0))
            repository.removeById(post.id)
            repository.insertLocal(PostEntity.fromDto(savedPost))
        } catch (_: Exception) {
            _state.value = _state.value?.copy(
                error = true
            )
        }
    }

    fun editPost(id: Long) {
        val postToEdit = data.value?.posts?.find { it.id == id }
        if (postToEdit != null) {
            edited.value = postToEdit
        }
    }
}