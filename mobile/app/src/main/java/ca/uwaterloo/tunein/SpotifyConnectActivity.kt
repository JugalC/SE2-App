package ca.uwaterloo.tunein

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SpotifyConnectActivity : ComponentActivity() {

    // handler function for the custom tab return
    private val intentLauncher =
        this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")
            Log.i("SpotifyConnectActivity", "onActivityResult")
            Log.i("SpotifyConnectActivity", "${result.resultCode}")

            // check if user is authed
            Log.i("SpotifyConnectActivity", AuthManager.getUser(this).toString())

            val queue = Volley.newRequestQueue(this)
            val url = "${BuildConfig.BASE_URL}/user/spotifyauth/${AuthManager.getUser(this).username}"

            val req = JSONObject()

            val userAuthCheck = JsonObjectRequest(
                Request.Method.GET, url, req,
                { res ->
                    // persist logged in state
                    Log.i("SpotifyConnectActivity", res.toString())
                    // check if is now set to authenticated,
                    // if so, send to Posts
                    if (res.getBoolean("authenticated")) {
                        Log.i("SpotifyConnectActivity", "authenticated")
                        AuthManager.setSpotifyAuthed(this,true)
                        val intent = Intent(this@SpotifyConnectActivity, PostsActivity::class.java)
                        startActivity(intent)
                    } else {
                        Log.i("SpotifyConnectActivity", "not authenticated")
                        // else, show error message
                        alert.setMessage("Authentication failed. Please try again.")
                        alert.create().show()
                    }
                },
                { error ->
                    Log.e("SpotifyConnectActivity", error.toString())
                    alert.setMessage("An unexpected error has occurred")
                    alert.create().show()

                }
            )
            queue.add(userAuthCheck)

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun initiateSpotifyConnect() {
            Log.i("SpotifyConnectActivity", "initiateSpotifyConnect")
            Log.i("SpotifyConnectActivity", "${AuthManager.isLoggedIn(this)}")
            Log.i("SpotifyConnectActivity", "${AuthManager.isSpotifyAuthed(this)}")

            val webpage: Uri =
                Uri.parse("http://10.0.2.2:8080/spotify/login/${AuthManager.getUser(this).username}")

            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.intent.setData(webpage)
            intentLauncher.launch(customTabsIntent.intent)

            Log.i("SpotifyConnectActivity", "launched url")
        }

        fun handleLogout() {
            AuthManager.setLoggedIn(this,false)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        setContent {
            SpotifyConnectScreen(::initiateSpotifyConnect, ::handleLogout)
        }
    }
}

@Composable
fun SpotifyConnectScreen( initiateSpotifyConnect: () -> Unit, handleLogout: () -> Unit ) {
    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Image(
                        painter = painterResource(id = R.drawable.tunein_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(69.dp)
                )
                Spacer(modifier = Modifier.width(90.dp)) // use this to center the logo
                Spacer(modifier = Modifier.height(164.dp))
                Text(
                    text = "Click the button below to connect to Spotify",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(64.dp))
                Button(
                    onClick = {
                        initiateSpotifyConnect()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                ) {
                    Text(text = "Connect to Spotify")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { handleLogout() },
                ) {
                    Text(
                        text = "Log Out"
                    )
                }
            }}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
        SpotifyConnectScreen ({}, {})
}