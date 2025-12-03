package ru.netology.nmedia.activity.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.viewModel.LoginViewModel

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.buttonSignIn.setOnClickListener {
            binding.login.error = null
            binding.password.error = null

            val login = binding.login.text.toString().trim()
            val pass = binding.password.text.toString().trim()

            if (login.isBlank()) {
                binding.login.error = getString(R.string.error_empty_login)
                return@setOnClickListener
            }

            if (pass.isBlank()) {
                binding.password.error = getString(R.string.error_empty_password)
                return@setOnClickListener
            }
            viewModel.authenticate(login, pass)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                binding.password.error = getString(R.string.error_auth_failed)
            }
            if (state.success) {
                findNavController().navigateUp()
            }
        }
        return binding.root
    }
}