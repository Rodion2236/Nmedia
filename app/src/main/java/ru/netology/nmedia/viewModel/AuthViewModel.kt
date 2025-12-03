package ru.netology.nmedia.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.AuthToken

class AuthViewModel: ViewModel() {
    val data: LiveData<AuthToken?> = AppAuth.getInstance()
        .authState
        .asLiveData()

    val isAuthorized: Boolean
        get() = data.value != null
}