package ru.netology.nmedia.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.AppActivityBinding
import ru.netology.nmedia.viewModel.AuthViewModel

class AppActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = AppActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        requestNotificationsPermission()

        intent?.let {
            if (it.action != Intent.ACTION_SEND) return@let

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(binding.root, R.string.error_empty_content, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        finish()
                    }
                    .show()
            }
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    putString("content", text)
                }
            )
        }

        addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    authViewModel.data.observe(this@AppActivity) {
                        val authorized = authViewModel.isAuthorized
                        menu.setGroupVisible(R.id.authorized, authorized)
                        menu.setGroupVisible(R.id.unauthorized, !authorized)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.signIn -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_loginFragment)
                            true
                        }
                        R.id.signUp -> {
                            findNavController(R.id.nav_host_fragment).navigate(R.id.action_feedFragment_to_registerFragment)
                            true
                        }
                        R.id.logout -> {
                            AlertDialog.Builder(this@AppActivity)
                                .setTitle(R.string.exit)
                                .setMessage(R.string.are_u_sure)
                                .setPositiveButton(R.string.yes_button) { _, _ ->
                                    AppAuth.getInstance().clear()
                                }
                                .setNegativeButton(R.string.no_button, null)
                                .show()
                            true
                        }
                        else -> false
                    }
            }
        )
    }
    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }
}