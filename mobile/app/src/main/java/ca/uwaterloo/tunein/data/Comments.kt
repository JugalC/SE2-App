package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val profilePicture: String = DEFAULT_PROFILE_PIC,
    val username: String = "",
    val content: String = ""
)


@Serializable
data class Comments(
    val comments: List<Comment> = listOf(Comment())
)