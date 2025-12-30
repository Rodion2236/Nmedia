package ru.netology.nmedia.activity.fragments

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegisterBinding
import ru.netology.nmedia.viewModel.RegisterViewModel
import java.io.File

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels {
        defaultViewModelProviderFactory
    }

    private var photoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegisterBinding.inflate(inflater, container, false)

        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == RESULT_OK && uri != null) {
                photoUri = uri
                binding.photoPreview.setImageURI(uri)
                binding.photoPreview.visibility = View.VISIBLE
            }
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent(imagePickerLauncher::launch)
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent(imagePickerLauncher::launch)
        }

        binding.buttonSignUp.setOnClickListener {
            binding.login.error = null
            binding.password.error = null
            binding.confirmPassword.error = null
            binding.name.error = null

            val login = binding.login.text.toString().trim()
            val pass = binding.password.text.toString().trim()
            val confirmPass = binding.confirmPassword.text.toString().trim()
            val name = binding.name.text.toString().trim()

            when {
                login.isBlank() -> binding.login.error = getString(R.string.error_empty_login)
                pass.isBlank() -> binding.password.error = getString(R.string.error_empty_password)
                pass != confirmPass -> binding.confirmPassword.error = getString(R.string.error_password_mismatch)
                name.isBlank() -> binding.name.error = getString(R.string.error_empty_name)
                else -> {
                    val photoFile = photoUri?.let { uri ->
                        val context = requireContext()
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val tempFile = File(context.cacheDir, "profile_photo.jpg")
                        tempFile.outputStream().use { output ->
                            inputStream?.copyTo(output)
                        }
                        tempFile
                    }
                    viewModel.register(name, login, pass, photoFile)
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                binding.password.error = getString(R.string.error_registration_failed)
            }
            if (state.success) {
                findNavController().navigateUp()
            }
        }

        return binding.root
    }
}