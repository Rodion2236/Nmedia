package ru.netology.nmedia.util

fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> "$count"
        count < 10_000 -> {
            val value = count / 100
            if (value % 10 == 0) {
                "${value / 10}K"
            } else {
                "${value / 10}.${value % 10}K"
            }
        }
        count < 1_000_000 -> "${count / 1_000}K"
        count < 10_000_000 -> {
            val value = count / 100_000
            if (value % 10 == 0) {
                "${value / 10}M"
            } else {
                "${value / 10}.${value % 10}M"
            }
        }
        else -> "${count / 1_000_000}M"
    }
}