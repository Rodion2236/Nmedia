package ru.netology.nmedia.repository

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostDaoImpl
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.PostMapper

class PostRepositorySQLiteImpl(
    private val db: SQLiteDatabase,
    private val dao: PostDao = PostDaoImpl()
) : PostRepository {

    private val _data = MutableLiveData<List<Post>>()
    override fun get(): LiveData<List<Post>> = _data

    init {
        loadPosts()
    }

    private fun loadPosts() {
        val cursor: Cursor = db.rawQuery(dao.getAll(), null)
        val posts = mutableListOf<Post>()
        while (cursor.moveToNext()) {
            posts.add(mapCursorToPost(cursor))
        }
        cursor.close()
        _data.value = posts
    }

    private fun mapCursorToPost(cursor: Cursor): Post = Post(
        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
        author = cursor.getString(cursor.getColumnIndexOrThrow("author")),
        content = cursor.getString(cursor.getColumnIndexOrThrow("content")),
        published = cursor.getString(cursor.getColumnIndexOrThrow("published")),
        likes = cursor.getInt(cursor.getColumnIndexOrThrow("likes")),
        likedByMe = cursor.getInt(cursor.getColumnIndexOrThrow("likedByMe")) == 1,
        shares = cursor.getInt(cursor.getColumnIndexOrThrow("shares")),
        views = cursor.getInt(cursor.getColumnIndexOrThrow("views")),
        video = cursor.getString(cursor.getColumnIndexOrThrow("video"))?.takeIf { it.isNotBlank() },
        viewed = cursor.getInt(cursor.getColumnIndexOrThrow("viewed")) == 1
    )

    override fun likeById(id: Long) {
        db.execSQL(dao.likeById(id))
        loadPosts()
    }

    override fun shareById(id: Long) {
        db.execSQL(dao.shareById(id))
        loadPosts()
    }

    override fun viewsById(id: Long) {
        _data.value = (_data.value ?: emptyList()).map { post ->
            if (post.id == id && !post.viewed) {
                post.copy(views = post.views + 1, viewed = true)
            } else {
                post
            }
        }
    }

    override fun removeById(id: Long) {
        db.execSQL(dao.removeById(id))
        loadPosts()
    }

    override fun save(post: Post) {
        val entity = PostMapper.toEntity(post)
        db.execSQL(dao.save(entity))
        loadPosts()
    }
}