package ca.uwaterloo.tunein.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.Post
import ca.uwaterloo.tunein.data.Profile
import ca.uwaterloo.tunein.data.ProfileResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class ProfileViewModel: ViewModel() {
    private val _profile = MutableStateFlow(Profile())
    val profile: StateFlow<Profile> = _profile.asStateFlow()
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    suspend fun getProfile(userId: String) = withContext(Dispatchers.IO) {
        val searchUrl = "${BuildConfig.BASE_URL}/profile_info/${userId}"
        val client = OkHttpClient()
        val request: okhttp3.Request = okhttp3.Request.Builder()
            .url(searchUrl)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val json = response.body.string()
        val profileResponse = Json.decodeFromString<ProfileResponse>(json)
        _profile.value = profileResponse.profile
        _posts.value = profileResponse.posts
    }

    suspend fun updateVisibility(post: Post, context: Context) = withContext(Dispatchers.IO) {
        val curVisibility = post.visible
        val updateUrl = "${BuildConfig.BASE_URL}/posts/visibility/${post.id}"
        val client = OkHttpClient()
        val body = JSONObject()
        body.put("action", if (curVisibility) "hide" else "show")
        val request: okhttp3.Request = okhttp3.Request.Builder()
            .url(updateUrl)
            .put(body.toString().toRequestBody())
            .addHeader("Authorization", "Bearer ${AuthManager.getAuthToken(context).toString()}")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val updatedPosts = _posts.value.map { p ->
                if (p.id == post.id) {
                    p.copy(visible = !p.visible)
                } else {
                    p
                }
            }
            _posts.value = updatedPosts
        }
    }
}