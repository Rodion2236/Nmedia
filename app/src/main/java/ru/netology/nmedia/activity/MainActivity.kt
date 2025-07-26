package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewmodel: PostViewModel by viewModels()

        val adapter = PostAdapter(
            onItemLikeListener = { postId ->
                viewmodel.like(postId)
            },
            onItemShareListener = { postId ->
                viewmodel.share(postId)
            },

            onItemViewListener = { postId ->
                viewmodel.views(postId)
            }
        )
        binding.list.adapter = adapter

//        viewmodel.views(posts.id)
        viewmodel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }
    }
}