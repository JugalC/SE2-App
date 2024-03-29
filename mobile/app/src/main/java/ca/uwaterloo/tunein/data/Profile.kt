package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String = "",
    val name: String = "",
    val albumName: String = "",
    val artists: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    var visible: Boolean,
)

@Serializable
data class Profile(
    val username: String = "",
    val firstName: String = "",
    val spotifyName: String = "",
    val friendsNum: Int = 0,
    val profilePic: String = DEFAULT_PROFILE_PIC,
    val created: String = "",
)

@Serializable
data class ProfileResponse(
    val profile: Profile = Profile(),
    var posts: List<Post> = emptyList()
)

