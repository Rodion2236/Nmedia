package ru.netology.nmedia.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    private val empty = Post(
        0,
        "",
        "",
        0,
        "",
        0,
    )

    suspend fun getById(id: Long): Post = repository.getById(id)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = appAuth.authState
        .onEach { authState ->
            Log.d("AUTH_REFRESH", "авторизация обновлена -> $authState")
        }
        .flatMapLatest { token ->
            repository.data.map { pagingData ->
                pagingData.map { post ->
                    post.copy(ownedByMe = token?.id == post.authorId)
                }
            }
        }
        .flowOn(Dispatchers.Default)

    val newerCount: LiveData<Int> = MutableLiveData(0)
//    val newerCount = data.switchMap {
//        repository.getNewer(it.posts.firstOrNull()?.id ?: 0)
//            .catch { _state.postValue(FeedModelState(error = true)) }
//            .asLiveData(Dispatchers.Default) }

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

    private val _showSignInDialog = SingleLiveEvent<Unit>()
    val showSignInDialog: LiveData<Unit>
        get() = _showSignInDialog

    private val _onAddNewPost = SingleLiveEvent<Unit>()
    val onAddNewPost: LiveData<Unit>
        get() = _onAddNewPost

    fun onAddButtonClicked() {
        if (!isAuthorized()) {
            _showSignInDialog.call()
        } else {
            _onAddNewPost.call()
        }
    }

    private fun isAuthorized(): Boolean {
        return appAuth.authState.value?.token != null
    }

    fun showNewPosts() = viewModelScope.launch {
        repository.showAllNewPosts()
    }

    fun like(id: Long) = viewModelScope.launch {
        try {
            if (!isAuthorized()) {
                _showSignInDialog.call()
                return@launch
            }

            repository.likeById(id)
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
                    withContext(Dispatchers.IO) {
                        try {
                            repository.save(
                                postToEdit.copy(content = trimmed),
                                _photo.value?.file)
                        } finally {
                            _photo.value?.file?.delete()
                        }
                    }
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

    fun editPost(post: Post) {
        edited.value = post
    }

    fun changePhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto() {
        _photo.value = null
    }
}