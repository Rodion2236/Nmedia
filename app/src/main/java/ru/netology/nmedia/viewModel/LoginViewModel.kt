package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject

data class LoginState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: PostApi,
    private val appAuth: AppAuth,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableLiveData(LoginState())
    val state: LiveData<LoginState> = _state

    fun authenticate(login: String, pass: String) = viewModelScope.launch(Dispatchers.Default) {
        try {
            _state.postValue(_state.value?.copy(loading = true, error = false))
            val response = apiService.authenticate(login, pass)

            val body = response.body() ?: throw RuntimeException("Empty response from server")
            appAuth.setAuth(body)

            _state.postValue(LoginState(success = true))
        } catch (_: Exception) {
            _state.postValue(_state.value?.copy(error = true, loading = false))
        }
    }
}