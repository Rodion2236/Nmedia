package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AppAuth
import java.io.File
import javax.inject.Inject

data class RegisterState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val success: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiService: PostApi,
    private val appAuth: AppAuth,
    application: Application
): AndroidViewModel(application) {

    private val _state = MutableLiveData(RegisterState())
    val state: LiveData<RegisterState> = _state

    fun register(name: String, login: String, pass: String, photoFile: File? = null) = viewModelScope.launch(Dispatchers.Default) {
        try {
            _state.postValue(_state.value?.copy(loading = true, error = false))

            val response = if (photoFile != null) {
                val loginBody = login.toRequestBody("text/plain".toMediaType())
                val passBody = pass.toRequestBody("text/plain".toMediaType())
                val nameBody = name.toRequestBody("text/plain".toMediaType())

                val mediaType = "image/*".toMediaType()
                val requestBody = photoFile.asRequestBody(mediaType)
                val part = MultipartBody.Part.createFormData("file", photoFile.name, requestBody)

                apiService.registerWithPhoto(loginBody, passBody, nameBody, part)
                } else {
                    apiService.registerUser(login, pass, name)
                }

            val body = response.body() ?: throw RuntimeException("Empty response")
            appAuth.setAuth(body)

            _state.postValue(RegisterState(success = true))
        } catch (_: Exception) {
            _state.postValue(_state.value?.copy(error = true, loading = false))
        }
    }
}