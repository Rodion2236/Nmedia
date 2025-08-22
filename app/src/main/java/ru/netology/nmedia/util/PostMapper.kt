package ru.netology.nmedia.util

import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostEntity

object PostMapper {
    fun toDto(entity: PostEntity) = Post(
        id = entity.id,
        author = entity.author,
        content = entity.content,
        published = entity.published,
        likes = entity.likes,
        likedByMe = entity.likedByMe,
        shares = entity.shares,
        views = entity.views,
        video = entity.video,
        viewed = entity.viewed
    )

    fun toEntity(post: Post) = PostEntity(
        id = post.id,
        author = post.author,
        content = post.content,
        published = post.published,
        likes = post.likes,
        likedByMe = post.likedByMe,
        shares = post.shares,
        views = post.views,
        video = post.video,
        viewed = post.viewed
    )
}