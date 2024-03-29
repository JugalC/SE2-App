package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

val DEFAULT_PROFILE_PIC = "https://builtprefab.com/wp-content/uploads/2019/01/cropped-blank-profile-picture-973460_960_720-300x300.png"

@Serializable
data class User(
    val id: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profilePicture: String = DEFAULT_PROFILE_PIC,
)

// Some tables store id as uId, so we must map it to properly deserialize the User object
class UserDeserializer : JsonTransformingSerializer<List<User>>(ListSerializer(User.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonArray) {
            val modifiedJsonArray = JsonArray(element.map { userElement ->
                if (userElement is JsonObject) {
                    JsonObject(userElement.toMutableMap().apply {
                        // Check if the JSON object contains 'uId' and map it to 'id' if it does
                        if (containsKey("uId")) {
                            remove("uId")?.let { put("id", it) }
                        }
                    })
                } else {
                    userElement
                }
            })
            return modifiedJsonArray
        } else if (element is JsonObject) {
            val modifiedJsonObject = JsonObject(element.toMutableMap().apply {
                // Check if the JSON object contains 'uId' and map it to 'id' if it does
                if (containsKey("uId")) {
                    remove("uId")?.let { put("id", it) }
                }
            })
            return modifiedJsonObject
        }
        return super.transformDeserialize(element)
    }
}
