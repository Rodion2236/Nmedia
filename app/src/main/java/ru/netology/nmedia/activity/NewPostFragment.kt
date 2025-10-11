package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.postIdArg
import ru.netology.nmedia.util.textArg
import ru.netology.nmedia.viewModel.PostViewModel

class NewPostFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels {
        defaultViewModelProviderFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        arguments?.textArg?.let {
            binding.content.setText(it)
            binding.content.setSelection(it.length)

            arguments?.postIdArg?.let { id ->
                if (id != 0L) {
                    viewModel.editPost(id)
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.add.setOnClickListener {
            val content = binding.content.text.toString().trim()
            if (content.isNotBlank()) {
                viewModel.save(content)
                findNavController().navigateUp()
            }
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
            viewModel.load()
        }
        return binding.root
    }
}