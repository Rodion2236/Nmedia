package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnPostInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentSinglePostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.postIdArg
import ru.netology.nmedia.util.textArg
import ru.netology.nmedia.viewModel.PostViewModel

class SinglePostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels {
        defaultViewModelProviderFactory
    }
    private var postId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.postIdArg ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSinglePostBinding.inflate(inflater, container, false)

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == postId }
            if (post != null) {
                PostViewHolder(binding.postLayout, object : OnPostInteractionListener {
                    override fun onLike(post: Post) {
                        viewModel.like(post.id)
                    }

                    override fun onShare(post: Post) {
                        viewModel.share(post.id)
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, post.content)
                        }
                        val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                        startActivity(chooser)
                    }

                    override fun onView(post: Post) {
                        viewModel.views(post.id)
                    }

                    override fun onRemove(post: Post) {
                        viewModel.remove(post.id)
                        findNavController().navigateUp()
                    }

                    override fun onEdit(post: Post) {
                        findNavController().navigate(
                            R.id.action_singlePostFragment_to_newPostFragment,
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
                    }
                }).bind(post)
            } else {
                findNavController().navigateUp()
            }
        }
        return binding.root
    }
}