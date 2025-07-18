package ru.netology.nmedia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.formatCount


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            likes = 0,
            likedByMe = false
        )

        post.views++

        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content

            like.setImageResource(if (post.likedByMe) R.drawable.ic_like else R.drawable.ic_empty_like)
            likeTv.text = formatCount(post.likes)
            shareTv.text = formatCount(post.shares)
            viewsTv.text = formatCount(post.views)

            like.setOnClickListener {
                post.likedByMe = !post.likedByMe
                post.likes += if (post.likedByMe) 1 else -1
                like.setImageResource(if (post.likedByMe) R.drawable.ic_like else R.drawable.ic_empty_like)
                likeTv.text = formatCount(post.likes)
            }

            share.setOnClickListener {
                post.shares++
                shareTv.text = formatCount(post.shares)
            }
        }
    }
}