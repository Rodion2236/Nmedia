package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.dto.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {

    @Query("SELECT MAX(`key`) FROM post_remote_keys")
    suspend fun max(): Long?

    @Query("SELECT MIN(`key`) FROM post_remote_keys")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(postRemoteKeyEntity: List<PostRemoteKeyEntity>)

    @Query("DELETE FROM post_remote_keys")
    suspend fun clear()
}