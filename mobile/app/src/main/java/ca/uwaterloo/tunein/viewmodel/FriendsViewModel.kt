package ca.uwaterloo.tunein.viewmodel

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


@Serializable
data class PendingInvites(
    val users: List<User> = emptyList()
)

class FriendsViewModel : ViewModel() {
    private val _pendingInvites = MutableStateFlow(PendingInvites())
    val pendingInvites: StateFlow<PendingInvites> = _pendingInvites.asStateFlow()
    private val _friends = MutableStateFlow(PendingInvites())
    val friends: StateFlow<PendingInvites> = _friends.asStateFlow()

    fun removePendingInvite(user: User) {
        val updatedUsers = _pendingInvites.value.users.toMutableList() // Make a mutable copy
        updatedUsers.remove(user) // Remove the user from the list
        _pendingInvites.value = _pendingInvites.value.copy(users = updatedUsers) // Update the StateFlow
    }

    fun getPendingInvites(user: User) {
        viewModelScope.launch {
            val users = getPendingFriendRequests(user)
            _pendingInvites.value = _pendingInvites.value.copy(
                users = users,
            )
        }
    }

    fun getCurrentFriends(user: User) {
        viewModelScope.launch {
            val users = getFriends(user)
            _friends.value = _friends.value.copy(
                users = users,
            )
        }
    }

}

suspend fun getFriends(user: User): List<User> = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/friendships/${user.id}"
    val request = okhttp3.Request(
        url = searchUrl.toHttpUrl(),
    )
    val response = OkHttpClient().newCall(request).execute()
    val json = response.body.string()
    val j = Json{ ignoreUnknownKeys = true }
    j.decodeFromString<List<User>>(json)
}

suspend fun getPendingFriendRequests(user: User): List<User> = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/friendship-requests/${user.id}"
    val request = okhttp3.Request(
        url = searchUrl.toHttpUrl()
    )
    val response = OkHttpClient().newCall(request).execute()

    val json = response.body.string()
    val j = Json{ ignoreUnknownKeys = true }
    j.decodeFromString<List<User>>(json)
}