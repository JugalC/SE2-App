package ca.uwaterloo.tunein.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.BuildConfig
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.data.PreviousPost
import ca.uwaterloo.tunein.data.Profile
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


class ProfileViewModel: ViewModel() {
    var returnedProfile = mutableStateOf(Profile())
    suspend fun updateReturnedProfile(userId: String) = withContext(Dispatchers.IO) {
        println("Called Function")
        val searchUrl = "${BuildConfig.BASE_URL}/profile_info/${userId}"
        val client = OkHttpClient()
        val request: okhttp3.Request = okhttp3.Request.Builder()
            .url(searchUrl)
            .get()
            .build()

        println("Finished builder")
        val response = client.newCall(request).execute()
        println("Finished response")
        val json = response.body.string()
        returnedProfile.value = Json.decodeFromString<Profile>(json)
//        returnedProfile.value = Profile("rishi-new", "Hrishit", "rishi_spot", 12,
//            "https://i.scdn.co/image/ab6775700000ee8519e0277b6134bd156bd45111",
//            listOf(PreviousPost("Keep The Family Close", "Views", "Drake",
//                "https://i.scdn.co/image/ab67616d00001e029416ed64daf84936d89e671c")))
    }
}