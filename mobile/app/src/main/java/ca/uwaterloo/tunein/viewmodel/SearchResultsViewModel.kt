package ca.uwaterloo.tunein.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

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
    val friendshipRequest: String?,
    val friendship: String?,
)

fun searchResultsToUser(user: SearchResults): User {
    return User (
        id = user.id,
        username = user.username,
        firstName = user.firstName,
        lastName = user.lastName
    )
}

suspend fun fetchSearchUsers(user: User, searchQuery: String): List<SearchResults> = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/friendships/search"
    val url = searchUrl.toHttpUrl()
        .newBuilder()
        .addQueryParameter("search", searchQuery)
        .addQueryParameter("id", user.id)

    val request = okhttp3.Request(
        url = url.build()
    )
    val response = OkHttpClient().newCall(request).execute()

    val json = response.body.string()
    val j = Json{ ignoreUnknownKeys = true }
    j.decodeFromString<List<SearchResults>>(json)
}

class SearchResultsViewModel : ViewModel() {
    private val _searchUsers = MutableStateFlow(SearchUsers())
    val searchUsers: StateFlow<SearchUsers> = _searchUsers.asStateFlow()

    fun updateSearchUsers(user: User, searchQuery: TextFieldValue) {
        viewModelScope.launch {
            val users = fetchSearchUsers(user, searchQuery.text).filter { u -> u.id != user.id }
            val friends = users.filter { u -> !u.friendship.isNullOrEmpty() }
            val activeFriendRequests = users.filter { u -> !u.friendshipRequest.isNullOrEmpty() }
            val rest = users.filter { u -> u.friendship.isNullOrEmpty() && u.friendshipRequest.isNullOrEmpty() }
            _searchUsers.value = _searchUsers.value.copy(
                users = rest,
                friendRequests = activeFriendRequests,
                friends = friends,
                searchQuery = searchQuery
            )
        }
    }
}