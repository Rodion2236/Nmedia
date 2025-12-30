package ru.netology.nmedia.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.AuthToken
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(appAuth: AppAuth): ViewModel() {
    val data: LiveData<AuthToken?> = appAuth
        .authState
        .asLiveData()

    val isAuthorized: Boolean
        get() = data.value != null
}