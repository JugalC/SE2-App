package ca.uwaterloo.tunein.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.SearchResults
import ca.uwaterloo.tunein.data.SearchUsers
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.data.UserDeserializer
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


@Serializable
data class Users(
    val users: List<User> = emptyList()
)

class FriendsViewModel : ViewModel() {
    private val _pendingInvites = MutableStateFlow(Users())
    val pendingInvites: StateFlow<Users> = _pendingInvites.asStateFlow()
    private val _friends = MutableStateFlow(Users())
    val friends: StateFlow<Users> = _friends.asStateFlow()
    private val _searchUsers = MutableStateFlow(SearchUsers())
    val searchUsers: StateFlow<SearchUsers> = _searchUsers.asStateFlow()

    fun removePendingInvite(user: User) {
        val updatedUsers = _pendingInvites.value.users.toMutableList() // Make a mutable copy
        updatedUsers.remove(user) // Remove the user from the list
        _pendingInvites.value =
            _pendingInvites.value.copy(users = updatedUsers) // Update the StateFlow
    }

    fun getPendingInvites(context: Context) {
        viewModelScope.launch {
            val users = getPendingFriendRequests(context)
            _pendingInvites.value = _pendingInvites.value.copy(
                users = users,
            )
        }
    }

    fun getCurrentFriends(context: Context) {
        viewModelScope.launch {
            val users = getFriends(context)
            _friends.value = _friends.value.copy(
                users = users,
            )
        }
    }

    fun removeFriend(user: User) {
        viewModelScope.launch {
            val updatedUsers = _friends.value.users.toMutableList() // Make a mutable copy
            updatedUsers.remove(user) // Remove the user from the list
            _friends.value =
                _friends.value.copy(users = updatedUsers) // Update the StateFlow
        }
    }

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

suspend fun getFriends(context: Context): List<User> = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/friendships"
    val client = OkHttpClient()
    val request: okhttp3.Request = okhttp3.Request.Builder()
        .url(searchUrl)
        .get()
        .addHeader("cache-control", "no-cache")
        .addHeader("Authorization", "Bearer ${AuthManager.getAuthToken(context).toString()}")
        .build()

    val response = client.newCall(request).execute()
    Log.i("FriendsViewModel", response.toString())
    val json = response.body.string()

    val j = Json { ignoreUnknownKeys = true }
    j.decodeFromString(UserDeserializer(), json)
}

suspend fun getPendingFriendRequests(context: Context): List<User> = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/friendship-requests"
    val client = OkHttpClient()
    val request: okhttp3.Request = okhttp3.Request.Builder()
        .url(searchUrl)
        .get()
        .addHeader("cache-control", "no-cache")
        .addHeader("Authorization", "Bearer ${AuthManager.getAuthToken(context).toString()}")
        .build()

    val response = client.newCall(request).execute()

    val json = response.body.string()
    val j = Json { ignoreUnknownKeys = true }
    j.decodeFromString(UserDeserializer(), json)
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
