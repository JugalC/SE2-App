package ca.uwaterloo.tunein.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.data.PreviousPost
import ca.uwaterloo.tunein.data.Feed
import ca.uwaterloo.tunein.data.UserDeserializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.serialization.decodeFromString


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