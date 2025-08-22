package ru.netology.nmedia.dao

import ru.netology.nmedia.dto.PostEntity

class PostDaoImpl : PostDao {
    override fun getAll(): String = """
        SELECT * FROM posts ORDER BY id DESC
    """.trimIndent()

    override fun save(post: PostEntity): String =
        if (post.id == 0L) {
            if (post.video != null) {
                """
            INSERT INTO posts (author, content, published, video) 
            VALUES ('${post.author}', '${post.content}', '${post.published}', '${post.video}')
            """.trimIndent()
            } else {
                """
            INSERT INTO posts (author, content, published) 
            VALUES ('${post.author}', '${post.content}', '${post.published}')
            """.trimIndent()
            }
        } else {
            if (post.video != null) {
                """
            UPDATE posts SET content = '${post.content}', video = '${post.video}' WHERE id = ${post.id}
            """.trimIndent()
            } else {
                """
            UPDATE posts SET content = '${post.content}' WHERE id = ${post.id}
            """.trimIndent()
            }
        }

    override fun removeById(id: Long): String = """
        DELETE FROM posts WHERE id = $id
    """.trimIndent()

    override fun likeById(id: Long): String = """
        UPDATE posts SET 
            likedByMe = CASE WHEN likedByMe = 1 THEN 0 ELSE 1 END,
            likes = likes + CASE WHEN likedByMe = 0 THEN 1 ELSE -1 END
        WHERE id = $id
    """.trimIndent()

    override fun shareById(id: Long): String = """
        UPDATE posts SET shares = shares + 1 WHERE id = $id
    """.trimIndent()

    override fun viewsById(id: Long): String = """
        UPDATE posts SET views = views + 1 WHERE id = $id
    """.trimIndent()

    override val createTableQuery: String = """
        CREATE TABLE posts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            author TEXT NOT NULL,
            content TEXT NOT NULL,
            published TEXT NOT NULL,
            likes INTEGER DEFAULT 0,
            likedByMe INTEGER DEFAULT 0,
            shares INTEGER DEFAULT 0,
            views INTEGER DEFAULT 0,
            video TEXT,
            viewed INTEGER DEFAULT 0
        );
    """.trimIndent()

    override val dropTableQuery: String = "DROP TABLE IF EXISTS posts"
}