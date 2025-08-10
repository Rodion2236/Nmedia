package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnPostInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.contracts.NewPostContract
import ru.netology.nmedia.contracts.NewPostResult
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewmodel: PostViewModel by viewModels()
        val newPostLauncher = registerForActivityResult(NewPostContract()) { result: NewPostResult? ->
            result ?: return@registerForActivityResult
            viewmodel.save(result.content, result.postId)
        }

        val adapter = PostAdapter(object : OnPostInteractionListener {
            override fun onLike(post: Post) {
                viewmodel.like(post.id)
            }

            override fun onShare(post: Post) {
                viewmodel.share(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
            }

            override fun onView(post: Post) {
                viewmodel.views(post.id)
            }

            override fun onRemove(post: Post) {
                viewmodel.remove(post.id)
            }

            override fun onEdit(post: Post) {
                newPostLauncher.launch(post)
            }

            override fun onVideoClick(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = post.video?.toUri()
                }
                startActivity(intent)
            }
        })

        binding.list.adapter = adapter

        viewmodel.data.observe(this) { posts ->
            val new = posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
            adapter.submitList(posts) {
                if (new) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        binding.add.setOnClickListener {
            newPostLauncher.launch(null)
        }
    }
}