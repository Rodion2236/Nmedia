package ru.netology.nmedia.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostDaoImpl

class PostDbHelper(
    context: Context,
    private val postDao: PostDao = PostDaoImpl()
) : SQLiteOpenHelper(context, "post.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(postDao.createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(postDao.dropTableQuery)
        onCreate(db)
    }
}