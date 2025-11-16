package ru.netology.nmedia.dto

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: Long,
    val author: String,
    val authorAvatar: String? = null,
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val video: String? = null,
    val viewed: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbeddable? = null,

    val ownedByMe: Boolean = true,
    val sent: Boolean = false,
    val timeSaved: Long,

    val showInFeed: Boolean = true
) {
    fun toDto() = Post(
        id,
        author,
        authorAvatar,
        content,
        published,
        likes,
        likedByMe,
        shares,
        viewed,
        views,
        video,
        attachment?.let {
            Attachment(
                url = it.url,
                description = it.description,
                type = AttachmentType.valueOf(it.type)
            )
        },
        sent,
        showInFeed
    )

    companion object {
        fun fromDto(dto: Post, showInFeed: Boolean = true) = PostEntity(
            dto.id,
            dto.author,
            dto.authorAvatar,
            dto.content,
            dto.published,
            dto.likes,
            dto.likedByMe,
            dto.shares,
            dto.views,
            dto.video,
            dto.viewed,
            dto.attachment?.let {
                AttachmentEmbeddable(
                    url = it.url,
                    description = it.description,
                    type = it.type.name
                )
            },

            ownedByMe = true,
            sent = true,
            timeSaved = dto.published,
            showInFeed = showInFeed
        )

        fun tempId(): Long = -System.currentTimeMillis()

        fun newLocalPost(content: String): PostEntity = PostEntity(
            id = tempId(),
            author = "Я",
            content = content,
            published = System.currentTimeMillis(),
            ownedByMe = true,
            sent = false,
            timeSaved = System.currentTimeMillis()
        )
    }
}
data class AttachmentEmbeddable(
    val url: String,
    val description: String?,
    val type: String
)