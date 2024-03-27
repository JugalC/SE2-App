package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable

@Serializable
data class PreviousPost(
    val name: String = "",
    val album_name: String = "",
    val artists: String = "",
    val image_url: String = "",
    val caption: String = ""
)

@Serializable
data class Profile(
    val username: String = "",
    val first_name: String = "",
    val spotify_name: String = "",
    val friends_num: Int = 0,
    val profile_pic: String = DEFAULT_PROFILE_PIC,
    val created: String = "",
    val previous_posts: List<PreviousPost> = listOf(PreviousPost())
)

