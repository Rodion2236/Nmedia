package ru.netology.nmedia.util

fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> "$count"
        count < 10_000 -> {
            val value = count / 1_000f
            if (value % 1 == 0f) {
                "${value.toInt()}K"
            } else {
                "${String.format("%.1f", value)}K"
            }
        }
        count < 1_000_000 -> "${count / 1_000}K"
        count < 10_000_000 -> {
            val value = count / 1_000_000f
            if (value % 1 == 0f) {
                "${value.toInt()}M"
            } else {
                "${String.format("%.1f", value)}M"
            }
        }
        else -> "${count / 1_000_000}M"
    }
}