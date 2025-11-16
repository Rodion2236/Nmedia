package ru.netology.nmedia.viewModel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.database.PostDatabase
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryNetwork
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val empty = Post(
        0,
        "",
        "",
        "",
        0,
    )
    private val repository: PostRepository = PostRepositoryNetwork(
        PostDatabase.getInstance(application).postDao()
    )
    val data: LiveData<FeedModel> = repository.data.map {
        FeedModel(
            it,
            it.isEmpty()
        )
    }.catch { it.printStackTrace() }.asLiveData(Dispatchers.Default)

    val newerCount = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0)
            .catch { _state.postValue(FeedModelState(error = true)) }
            .asLiveData(Dispatchers.Default) }

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

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

    fun showNewPosts() = viewModelScope.launch {
        repository.showAllNewPosts()
    }

    fun like(id: Long) = viewModelScope.launch {
        try {
            val currentPost = data.value?.posts?.find { it.id == id } ?: return@launch
            if (currentPost.likedByMe) {
                repository.unlikeById(id)
            } else {
                repository.likeById(id)
            }
        } catch (_: Exception) {
            _state.value = _state.value?.copy(
                error = true
            )
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
                    repository.save(postToSave, _photo.value?.file)
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

    fun changePhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto() {
        _photo.value = null
    }
}