package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.Request
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.AuthToken
import ru.netology.nmedia.dto.PushToken
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseMessaging: FirebaseMessaging
) {

    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"
    }

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _authState = MutableStateFlow<AuthToken?>(null)
    val authState = _authState.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0)

        if (token == null || id == 0L) {
            prefs.edit { clear() }
        } else {
            _authState.value = AuthToken(id, token)
        }
        sendPushToken()
    }

    @Synchronized
    fun setAuth(token: AuthToken) {
        _authState.value = token
        prefs.edit {
            putLong(ID_KEY, token.id)
            putString(TOKEN_KEY, token.token)
        }
        sendPushToken()
    }

    @Synchronized
    fun clear() {
        _authState.value = null
        prefs.edit { clear() }
        sendPushToken()
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(EmptyCoroutineContext).launch {
            runCatching {
                val finalToken = token ?: firebaseMessaging.token.await()
                val apiService = EntryPointAccessors.fromApplication(
                    context,
                    ApiServiceEntryPoint::class.java
                ).getApiService()
                apiService.sendPushToken(PushToken(finalToken))
            }
                .onFailure { it.printStackTrace() }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppAuthAccessorEntryPoint {
    fun getAppAuth(): AppAuth
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApiServiceEntryPoint {
    fun getApiService(): PostApi
}