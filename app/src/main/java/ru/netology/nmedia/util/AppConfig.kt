package ru.netology.nmedia.util

import ru.netology.nmedia.BuildConfig

object AppConfig {
    private const val BASE_URL = BuildConfig.BASE_URL

    const val AVATAR_BASE_URL = "$BASE_URL/avatars/"
    const val IMAGE_BASE_URL = "$BASE_URL/media/"
}