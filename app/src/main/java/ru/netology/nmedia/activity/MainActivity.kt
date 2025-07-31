package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnPostInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewmodel: PostViewModel by viewModels()

        val adapter = PostAdapter( object : OnPostInteractionListener {
            override fun onLike(post: Post) {
                viewmodel.like(post.id)
            }

            override fun onShare(post: Post) {
                viewmodel.share(post.id)
            }

            override fun onView(post: Post) {
                viewmodel.views(post.id)
            }

            override fun onRemove(post: Post) {
                viewmodel.remove(post.id)
            }

            override fun onEdit(post: Post) {
                viewmodel.edit(post)
            }

        }
        )
        binding.list.adapter = adapter

        viewmodel.data.observe(this) { posts ->
            val new = posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
            adapter.submitList(posts) {
                if (new) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        viewmodel.edited.observe(this) { post ->
            if (post.id == 0L) {
                binding.editingPanel.visibility = View.GONE
                binding.content.hint = getString(R.string.post_text)
            } else {
                binding.editingPanel.visibility = View.VISIBLE
                binding.content.hint = getString(R.string.edit_post_hint)
                if (binding.content.text.toString() != post.content) {
                    binding.content.setText(post.content)
                    binding.content.setSelection(binding.content.text.length)
                }
                AndroidUtils.showKeyboard(binding.content)
            }
        }

        binding.save.setOnClickListener {
            val text = binding.content.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this@MainActivity, R.string.error_empty_content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewmodel.save(text)
            binding.content.setText("")
            binding.content.clearFocus()

            AndroidUtils.hideKeyboard(binding.content)
        }

        binding.cancelEdit.setOnClickListener {
            viewmodel.cancelEdit()
            binding.content.setText("")
            binding.content.clearFocus()
            AndroidUtils.hideKeyboard(binding.content)
        }
    }
}