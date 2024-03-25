package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

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
    val profile_pic: String = "",
    val created: String = "",
    val previous_posts: List<PreviousPost> = listOf(PreviousPost())
)

