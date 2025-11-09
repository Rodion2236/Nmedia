package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE showInFeed = 1 ORDER BY timeSaved DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("UPDATE posts SET showInFeed = 1 WHERE showInFeed = 0")
    suspend fun showAllNewPosts()

    @Query("SELECT COUNT(*) FROM posts WHERE showInFeed = 0")
    suspend fun getHiddenPostsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Update
    suspend fun update(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE posts SET likedByMe = CASE WHEN likedByMe = 1 THEN 0 ELSE 1 END, likes = likes + CASE WHEN likedByMe = 1 THEN -1 ELSE 1 END WHERE id = :id")
    suspend fun toggleLike(id: Long)

    @Query("UPDATE posts SET shares = shares + 1 WHERE id = :id")
    suspend fun shareById(id: Long)

    @Query("UPDATE posts SET views = views + 1, viewed = 1 WHERE id = :id AND viewed = 0")
    suspend fun viewsById(id: Long)
}