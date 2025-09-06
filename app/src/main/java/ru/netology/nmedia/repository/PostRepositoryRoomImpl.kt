package ru.netology.nmedia.repository

import ru.netology.nmedia.util.PostMapper
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post

class PostRepositoryRoomImpl(private val dao: PostDao) : PostRepository {

    override fun get(): LiveData<List<Post>> {
        return dao.getAll().map { entities ->
            entities.map(PostMapper::toDto)
        }
    }

    override fun likeById(id: Long) {
        Thread { dao.likeById(id) }.start()
    }

    override fun shareById(id: Long) {
        Thread { dao.shareById(id) }.start()
    }

    override fun viewsById(id: Long) {
        Thread { dao.viewsById(id) }.start()
    }

    override fun removeById(id: Long) {
        Thread { dao.deleteById(id) }.start()
    }

    override fun save(post: Post) {
        Thread {
            val entity = PostMapper.toEntity(post)
            if (entity.id == 0L) {
                dao.insert(entity.copy(id = System.currentTimeMillis()))
            } else {
                dao.update(entity)
            }
        }.start()
    }
}