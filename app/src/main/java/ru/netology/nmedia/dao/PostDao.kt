package ru.netology.nmedia.dao

import ru.netology.nmedia.dto.PostEntity

interface PostDao {
    fun getAll(): String
    fun save(post: PostEntity): String
    fun removeById(id: Long): String
    fun likeById(id: Long): String
    fun shareById(id: Long): String
    fun viewsById(id: Long): String

    val createTableQuery: String
    val dropTableQuery: String
}