package ru.netology.nmedia.contracts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ru.netology.nmedia.activity.NewPostActivity
import ru.netology.nmedia.dto.Post

data class NewPostResult(val content: String, val postId: Long)

class NewPostContract : ActivityResultContract<Post?, NewPostResult?>() {

    override fun createIntent(context: Context, post: Post?): Intent {
        return Intent(context, NewPostActivity::class.java).apply {
            post?.let {
                putExtra("post_id", it.id)
                putExtra("post_content", it.content)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): NewPostResult? {
        if (resultCode != android.app.Activity.RESULT_OK) return null
        val content = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: return null
        val postId = intent.getLongExtra("post_id", 0)
        return NewPostResult(content, postId)
    }
}