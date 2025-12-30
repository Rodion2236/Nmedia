package ru.netology.nmedia.activity.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.postIdArg
import ru.netology.nmedia.util.textArg
import ru.netology.nmedia.viewModel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels()
    private val MAX_SIZE_PX = 2048

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        arguments?.textArg?.let {
            binding.content.setText(it)
            binding.content.setSelection(it.length)
        }

        viewModel.data.observe(viewLifecycleOwner) { feedModel ->
            val postId = arguments?.postIdArg
            if (postId != null && postId != 0L) {
                val postToEdit = feedModel.posts.find { it.id == postId }
                if (postToEdit != null) {
                    viewModel.editPost(postToEdit)
                    arguments?.clear()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            val content = binding.content.text.toString().trim()
                            if (content.isNotBlank()) {
                                viewModel.save(content)
                                findNavController().navigateUp()
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
            },
            viewLifecycleOwner
        )

        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.image_picker_error),
                    Toast.LENGTH_SHORT).show()
            } else if (uri != null) {
                viewModel.changePhoto(uri, uri.toFile())
            }
        }

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo != null) {
                binding.previewContainer.isVisible = true
                binding.preview.setImageURI(photo.uri)
            } else {
                binding.previewContainer.isVisible = false
            }
        }

        binding.removePhoto.setOnClickListener {
            viewModel.removePhoto()
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .maxResultSize(MAX_SIZE_PX, MAX_SIZE_PX)
                .createIntent(imagePickerLauncher::launch)
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .maxResultSize(MAX_SIZE_PX, MAX_SIZE_PX)
                .createIntent(imagePickerLauncher::launch)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
            viewModel.load()
        }
        return binding.root
    }
}