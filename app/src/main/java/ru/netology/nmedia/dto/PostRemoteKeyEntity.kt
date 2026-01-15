package ru.netology.nmedia.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_remote_keys")
data class PostRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val key: Long,
) {
    enum class KeyType {
        AFTER,
        BEFORE,
    }
}