package ru.netology.nmedia.activity.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnPostInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.postIdArg
import ru.netology.nmedia.util.textArg
import ru.netology.nmedia.viewModel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewmodel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

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
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                        postIdArg = post.id
                    }
                )
            }

            override fun onVideoClick(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = post.video?.toUri()
                }
                startActivity(intent)
            }

            override fun onPostClick(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_singlePostFragment,
                    Bundle().apply {
                        postIdArg = post.id
                    }
                )
            }

            override fun onRetrySend(post: Post) {
                viewmodel.retrySend(post)
            }

            override fun onImageClick(url: String) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageViewerFragment,
                    Bundle().apply { putString("imageUrl", url) }
                )
            }
        })

        binding.list.adapter = adapter

        viewmodel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.empty.isVisible = state.empty
        }

        viewmodel.newerCount.observe(viewLifecycleOwner) { count ->
            binding.showNewPosts.isVisible = count > 0
        }

        binding.showNewPosts.setOnClickListener {
            binding.showNewPosts.isVisible = false
            viewmodel.showNewPosts()

            binding.list.post {
                binding.list.smoothScrollToPosition(0)
            }
        }

        viewmodel.state.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.something_went_wrong, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry) {
                        viewmodel.load()
                    }
                    .show()
            }
            binding.swipeRefresh.isRefreshing = state.loading
        }

        viewmodel.onAddNewPost.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        viewmodel.showSignInDialog.observe(viewLifecycleOwner) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.login_required)
                .setMessage(R.string.action_log_to_acc)
                .setPositiveButton(R.string.login) { _, _ ->
                    findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewmodel.load()
        }

        binding.add.setOnClickListener {
            viewmodel.onAddButtonClicked()
        }
        return binding.root
    }
}