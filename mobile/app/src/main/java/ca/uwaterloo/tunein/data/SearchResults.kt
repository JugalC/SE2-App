package ca.uwaterloo.tunein.data

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Serializable

data class SearchUsers(
    val searchQuery: TextFieldValue = TextFieldValue(),
    val users: List<SearchResults> = emptyList(),
    val friends: List<SearchResults> = emptyList(),
    val friendRequests: List<SearchResults> = emptyList(),
)

@Serializable
data class SearchResults(
    val id: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profilePicture: String = DEFAULT_PROFILE_PIC,
    val friendshipRequest: String?,
    val friendship: String?,
)

fun searchResultsToUser(user: SearchResults): User {
    return User (
        id = user.id,
        username = user.username,
        firstName = user.firstName,
        lastName = user.lastName,
        profilePicture = user.profilePicture,
    )
}