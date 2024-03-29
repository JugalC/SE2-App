package ca.uwaterloo.tunein.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.data.Feed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient


class FeedViewModel: ViewModel() {
    var returnedFeed = mutableStateOf(Feed())
    suspend fun updateReturnedFeed(userId: String) = withContext(Dispatchers.IO) {
        val searchUrl = "${BuildConfig.BASE_URL}/feed/${userId}"
        val client = OkHttpClient()
        val request: okhttp3.Request = okhttp3.Request.Builder()
            .url(searchUrl)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val json = response.body.string()
        returnedFeed.value = Json.decodeFromString<Feed>(json)
    }
}