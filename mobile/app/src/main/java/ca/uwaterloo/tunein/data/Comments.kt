package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String = "",
    val postId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Long = 0
)


@Serializable
data class Comments(
    val posts: List<Comment> = listOf(Comment())
)