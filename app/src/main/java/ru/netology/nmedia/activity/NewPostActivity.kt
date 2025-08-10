package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityNewPostBinding

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val postId = intent.getLongExtra("post_id", 0)
        val content = intent.getStringExtra("post_content")

        if (content != null) {
            binding.content.setText(content)
            binding.content.setSelection(content.length)
        }

        binding.add.setOnClickListener {
            val text = binding.content.text.toString().trim()
            if (text.isBlank()) {
                setResult(RESULT_CANCELED)
            } else {
                val resultIntent = Intent().apply {
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra("post_id", postId)
                }
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }
    }
}