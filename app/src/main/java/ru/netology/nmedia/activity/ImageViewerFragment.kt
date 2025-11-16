package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentImageViewerBinding
import ru.netology.nmedia.util.AppConfig

class ImageViewerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageViewerBinding.inflate(inflater, container, false)

        val mainToolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        mainToolbar.visibility = View.GONE

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val imageUrl = arguments?.getString("imageUrl") ?: return binding.root

        Glide.with(binding.imageView)
            .load("${AppConfig.IMAGE_BASE_URL}$imageUrl")
            .placeholder(R.drawable.ic_load_24)
            .error(R.drawable.ic_broken_24)
            .timeout(10_000)
            .into(binding.imageView)

        return binding.root
    }

    override fun onDestroyView() {
        try {
            requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).visibility = View.VISIBLE
        } catch (_: Exception) {}
        super.onDestroyView()
    }
}