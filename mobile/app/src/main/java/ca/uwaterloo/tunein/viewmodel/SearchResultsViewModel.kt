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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

data class SearchUsers(
    val searchQuery: TextFieldValue = TextFieldValue(),
    val users: List<User> = emptyList()
)

suspend fun fetchSearchUsers(searchQuery: String): List<User> = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/users?search=${searchQuery}"
    val request = okhttp3.Request(
        url = searchUrl.toHttpUrl()
    )
    val response = OkHttpClient().newCall(request).execute()

    val json = response.body?.string()
    val j = Json{ ignoreUnknownKeys = true }
    j.decodeFromString<List<User>>(json!!)
}

class SearchResultsViewModel : ViewModel() {
    private val _searchUsers = MutableStateFlow(SearchUsers())
    val searchUsers: StateFlow<SearchUsers> = _searchUsers.asStateFlow()

    fun updateSearchUsers(user: User, searchQuery: TextFieldValue) {
        viewModelScope.launch {
            val users = fetchSearchUsers(searchQuery.text).filter { u -> u.id != user.id }
            _searchUsers.value = _searchUsers.value.copy(
                users = users,
                searchQuery = searchQuery
            )
        }
    }
}