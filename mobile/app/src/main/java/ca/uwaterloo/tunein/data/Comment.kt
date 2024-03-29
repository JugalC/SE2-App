package ca.uwaterloo.tunein.data

data class Comment(
    val id: String,
    val postId: String,
    val username: String,
    val text: String,
    val timestamp: Long
)
