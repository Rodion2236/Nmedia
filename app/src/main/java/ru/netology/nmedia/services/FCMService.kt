package ru.netology.nmedia.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.json.JSONObject
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val gson = Gson()
    private val channelId = "remote"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i("fcm msg", message.data.toString())
        val rawContent = message.data["content"] ?: return
        val recipientId = try {
            val json = JSONObject(rawContent)
            when (val id = json.get("recipientId")) {
                is Int -> id.toLong()
                is Long -> id
                else -> null
            }
        } catch (e: Exception) {
            Log.e("FCM", "Can't parse recipientId from content", e)
            null
        }

        when {
            // when recipientId == myRecipientId
            recipientId == AppAuth.getInstance().authState.value?.id -> {
                message.data[action]?.let { action ->
                    when (Action.fromValue(action)) {
                        Action.LIKE -> handleLike(
                            gson.fromJson(message.data[content], Like::class.java)
                        )
                        Action.NEW_POST -> handleNewPost(
                            gson.fromJson(message.data[content], NewPost::class.java)
                        )
                        null -> Log.w("FCM", "Неизвестное действие: $action")
                    }
                }
            }

            // when recipientId == 0
            recipientId == 0L -> {
                Log.d("FCM", "✅ recipientId = 0 -> resend push token")
                AppAuth.getInstance().sendPushToken()
            }

            // when recipientId != 0 и != myRecipientId
            recipientId != null && recipientId != 0L && recipientId != AppAuth.getInstance().authState.value?.id -> {
                Log.d("FCM", "✅ recipientId=$recipientId -> resend push token")
                AppAuth.getInstance().sendPushToken()
            }

            // when recipientId == null
            recipientId == null -> {
                message.data[action]?.let { action ->
                    when (Action.fromValue(action)) {
                        Action.LIKE -> handleLike(
                            gson.fromJson(message.data[content], Like::class.java)
                        )
                        Action.NEW_POST -> handleNewPost(
                            gson.fromJson(message.data[content], NewPost::class.java)
                        )
                        null -> Log.w("FCM", "Неизвестное действие: $action")
                    }
                }
            } else -> {
                Log.d("FCM", "Ignored message with recipientId: $recipientId")
            }
        }
    }

    private fun handleLike(like: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    like.userName,
                    like.postAuthor
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notify(notification)
    }

    private fun handleNewPost(newPost: NewPost) {
        val bigText = NotificationCompat.BigTextStyle().bigText(newPost.content)
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(
                getString(
                    R.string.notification_new_post_title,
                    newPost.author
                )
            )
            .setContentText(newPost.content)
            .setStyle(bigText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        notify(notification)
    }

    private fun notify(notification: Notification) {
        val isUpperTiramisu = Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU
        val isPostNotificationGranted = if (isUpperTiramisu) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        if (isPostNotificationGranted) {
            NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
        }
    }

    override fun onNewToken(token: String) {
        Log.i("fcm token", token)
        AppAuth.getInstance().sendPushToken(token)
    }

    enum class Action {
        LIKE,
        NEW_POST;

        companion object { // возвращает null при неизвестной ошибке, логирует, не падает
            fun fromValue(value: String?): Action? = Action.entries.find { it.name == value }
        }
    }

    data class Like(
        val userId: Long,
        val userName: String,
        val postId: Long,
        val postAuthor: String,
    )

    data class NewPost(
        val author: String,
        val content: String
    )
}