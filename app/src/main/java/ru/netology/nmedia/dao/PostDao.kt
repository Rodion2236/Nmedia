package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.netology.nmedia.dto.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: PostEntity)

    @Update
    fun update(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    fun deleteById(id: Long)

    @Query("UPDATE posts SET likes = likes + 1, likedByMe = 1 WHERE id = :id")
    fun likeById(id: Long)

    @Query("UPDATE posts SET likes = likes - 1, likedByMe = 0 WHERE id = :id AND likedByMe = 1")
    fun unlikeById(id: Long)

    @Query("UPDATE posts SET shares = shares + 1 WHERE id = :id")
    fun shareById(id: Long)

    @Query("UPDATE posts SET views = views + 1, viewed = 1 WHERE id = :id AND viewed = 0")
    fun viewsById(id: Long)
}