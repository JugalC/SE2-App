package ca.uwaterloo.tunein.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.data.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient


class FeedViewModel: ViewModel() {
    private val _feed = MutableStateFlow(Feed())
    val feed: StateFlow<Feed> = _feed.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun updateReturnedFeed(userId: String) {
        viewModelScope.launch {
            val newFeed = updateFeedData(userId)
            _feed.value = _feed.value.copy(
                posts = newFeed.posts,
            )
            _isRefreshing.emit(false)
        }
    }
}
suspend fun updateFeedData(userId: String) = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/feed/${userId}"
    val client = OkHttpClient()
    val request: okhttp3.Request = okhttp3.Request.Builder()
        .url(searchUrl)
        .get()
        .build()

    val response = client.newCall(request).execute()
    val json = response.body.string()
    Json.decodeFromString<Feed>(json)
}
