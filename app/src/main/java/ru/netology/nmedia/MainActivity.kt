package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.util.formatCount
import ru.netology.nmedia.viewModel.PostViewModel


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewmodel: PostViewModel by viewModels()

        viewmodel.views()
        viewmodel.data.observe(this) { post ->

            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content

                like.setImageResource(if (post.likedByMe) R.drawable.ic_like else R.drawable.ic_empty_like)

                likeTv.text = formatCount(post.likes)
                shareTv.text = formatCount(post.shares)
                viewsTv.text = formatCount(post.views)
            }
        }

            binding.like.setOnClickListener {
                viewmodel.like()
            }

            binding.share.setOnClickListener {
                viewmodel.share()
            }
    }
}