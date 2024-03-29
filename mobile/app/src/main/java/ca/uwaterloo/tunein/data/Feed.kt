package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable

@Serializable
data class SinglePost(
    val id: String = "",
    val user_id: String = "",
    val profile_picture: String = DEFAULT_PROFILE_PIC,
    val username: String = "",
    val name: String = "",
    val album_name: String = "",
    val artists: String = "",
    val image_url: String = "",
    val spotify_url: String = ""
)

@Serializable
data class Feed(
    val posts: List<SinglePost> = listOf(SinglePost())
)