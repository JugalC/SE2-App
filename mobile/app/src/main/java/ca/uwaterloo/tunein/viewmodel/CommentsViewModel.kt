package ca.uwaterloo.tunein.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.ProfileActivity
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.Comment
import ca.uwaterloo.tunein.data.Comments
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
import okhttp3.Request


class CommentsViewModel : ViewModel() {
    private val _comments = MutableStateFlow<Comments>(Comments())
    val comments: StateFlow<Comments> = _comments.asStateFlow()

    init {
        // Sample data
        val sampleComments = Comments(
            listOf(
                Comment(
                    id = "1",
                    postId = "post1",
                    userId = "User1",
                    content = "This is a sample comment."
                ),
                Comment(
                    id = "2",
                    postId = "post1",
                    userId = "User2",
                    content = "lol jdawg is so funny"
                ),
                Comment(
                    id = "3",
                    postId = "post1",
                    userId = "User3",
                    content = "great taste in music jdawg, *ok emoji*"
                ),
            )
        )
        viewModelScope.launch {
            _comments.emit(sampleComments)
        }
    }

    fun pullComments(postId: String, context: Context) {
        Log.i("CommentsViewModel", "pullComments: $postId")
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
    Log.i("CommentsViewModel", "getCommentsData: $json")
    Json.decodeFromString<Comments>(json)
}
//    fun updateReturnedFeed(userId: String) {
//        viewModelScope.launch {
//            val newFeed = updateFeedData(userId)
//            _feed.value = _feed.value.copy(
//                posts = newFeed.posts,
//            )
//            _isRefreshing.emit(false)
//        }
//    }
//    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
//    val comments: StateFlow<List<Comment>> = _comments
//

//    fun addComment(postId: String, text: String) {
//        val newComment = Comment(
//            id = (comments.value.size + 1).toString(),
//            postId = postId,
//            username = "CurrentUsername",
//            text = text,
//            timestamp = System.currentTimeMillis()
//        )
//
//        // Add the new comment to the existing list
//        val updatedComments = comments.value.toMutableList().apply {
//            add(newComment)
//        }
//        viewModelScope.launch {
//            _comments.emit(updatedComments)
//        }
//    }
//
//    fun getCommentsForPost(postId: String): StateFlow<List<Comment>> {
//        //fetch comments from backend
//        return comments
//    }
//}


//class FeedViewModel: ViewModel() {
//    private val _feed = MutableStateFlow(Feed())
//    val feed: StateFlow<Feed> = _feed.asStateFlow()
//    private val _isRefreshing = MutableStateFlow(false)
//    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
//
//    fun updateReturnedFeed(userId: String) {
//        viewModelScope.launch {
//            val newFeed = updateFeedData(userId)
//            _feed.value = _feed.value.copy(
//                posts = newFeed.posts,
//            )
//            _isRefreshing.emit(false)
//        }
//    }
//}
//suspend fun updateFeedData(userId: String) = withContext(Dispatchers.IO) {
//    val searchUrl = "${BuildConfig.BASE_URL}/feed/${userId}"
//    val client = OkHttpClient()
//    val request: Request = Request.Builder()
//        .url(searchUrl)
//        .get()
//        .build()
//
//    val response = client.newCall(request).execute()
//    val json = response.body.string()
//    Json.decodeFromString<Feed>(json)
//}
