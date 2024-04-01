package ca.uwaterloo.tunein.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.Feed
import ca.uwaterloo.tunein.data.Post
import ca.uwaterloo.tunein.helpers.updateVisibilityRequest
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
    private val _showBanner = MutableStateFlow(true)
    val showBanner = _showBanner.asStateFlow()
    private val _mostRecentPost = MutableStateFlow(Post())
    val mostRecentPost = _mostRecentPost.asStateFlow()

    fun updateReturnedFeed(userId: String) {
        viewModelScope.launch {
            val newFeed = updateFeedData(userId)
            _feed.value = _feed.value.copy(
                posts = newFeed.posts,
            )
            _isRefreshing.emit(false)
        }
    }

    fun shouldShowPostBanner(context: Context) {
        viewModelScope.launch {
            val post = getMostRecentPost(context)
            if (!post.userViewed) {
                _mostRecentPost.value = post
                _showBanner.value = true
            } else {
                _showBanner.value = false
            }
        }
    }

    fun updatePostVisibility(context: Context, visible: Boolean) {
        viewModelScope.launch {
            val response = updateVisibilityRequest(_mostRecentPost.value, visible, context)
            if (response.isSuccessful) {
                _showBanner.value = false
            }
        }
    }
}

suspend fun getMostRecentPost(context: Context) = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/posts/recent"
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
    j.decodeFromString<Post>(json)
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
