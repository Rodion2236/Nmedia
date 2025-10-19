package ru.netology.nmedia.util

import java.text.SimpleDateFormat
import java.util.*

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

object Utils {
    fun formatPublished(published: Long): String {
        val currentMillis = System.currentTimeMillis()
        val postMillis = published * 1000
        val diff = currentMillis - postMillis

        val minuteInMillis = 60_000L
        val hourInMillis = 3_600_000L
        val dayInMillis = 86_400_000L

        return when {
            diff < minuteInMillis -> "только что"
            diff < hourInMillis -> "${diff / minuteInMillis} мин назад"
            diff < dayInMillis -> "${diff / hourInMillis} ч назад"
            diff < 2 * dayInMillis -> "вчера"
            else -> {
                val formatter = SimpleDateFormat("d MMM yy", Locale.getDefault())
                formatter.format(Date(postMillis))
            }
        }
    }
}