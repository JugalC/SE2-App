package ca.uwaterloo.tunein.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.Comments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request


class CommentsViewModel : ViewModel() {
    private val _comments = MutableStateFlow<Comments>(Comments())
    val comments: StateFlow<Comments> = _comments.asStateFlow()

    fun pullComments(postId: String, context: Context) {
        viewModelScope.launch {
            val commentsResponse = getCommentsData(postId, context)
            _comments.value = _comments.value.copy(
                comments = commentsResponse.comments,
            )
        }
    }
}

suspend fun getCommentsData(postId: String, context: Context) = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/comments/${postId}"

    val client = OkHttpClient()

    val request: Request = Request.Builder()
        .url(searchUrl)
        .get()
        .addHeader("Authorization", "Bearer ${AuthManager.getAuthToken(context).toString()}")
        .addHeader("Content-Type", "application/json")
        .build()


    val response = client.newCall(request).execute()
    val json = response.body.string()
    Json.decodeFromString<Comments>(json)
}
