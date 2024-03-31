package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable

@Serializable
data class FeedPost(
    val id: String = "",
    val userId: String = "",
    val profilePicture: String = DEFAULT_PROFILE_PIC,
    val username: String = "",
    val name: String = "",
    val albumName: String = "",
    val artists: String = "",
    val imageUrl: String = "",
    val spotifyUrl: String = "",
    val commentsNum: Int = 0
)

@Serializable
data class Feed(
    val posts: List<FeedPost> = listOf(FeedPost())
)