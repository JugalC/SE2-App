package ca.uwaterloo.tunein.helpers

import android.content.Context
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

suspend fun updateVisibilityRequest(post: Post, visible: Boolean, context: Context) = withContext(Dispatchers.IO) {
    val updateUrl = "${BuildConfig.BASE_URL}/posts/visibility/${post.id}"
    val client = OkHttpClient()
    val body = JSONObject()
    body.put("action", if (visible) "hide" else "show")
    val request: Request = Request.Builder()
        .url(updateUrl)
        .put(body.toString().toRequestBody())
        .addHeader("Authorization", "Bearer ${AuthManager.getAuthToken(context).toString()}")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute()
}
