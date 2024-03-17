package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.messaging.Firebase
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

data class LoginState(
    var username: String = "",
    var password: String = "",
)

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        fun goBack() {
            super.finish()
        }

        fun handleLogin(loginState: LoginState) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")

            val queue = Volley.newRequestQueue(this)
            val loginUrl = "${BuildConfig.BASE_URL}/login/${loginState.username}"

            val req = JSONObject()
            req.put("password", loginState.password)

            val loginReq = JsonObjectRequest(
                Request.Method.POST, loginUrl, req,
                { loginRes ->
                    // persist logged in state
                    AuthManager.setAuthToken(this, loginRes.getString("token"))
                    val user = User(
                        id=loginRes.getString("id"),
                        username=loginRes.getString("username"),
                        firstName=loginRes.getString("firstName"),
                        lastName=loginRes.getString("lastName")
                    )
                    AuthManager.setUser(this, user)

                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w("TuneInFirebaseMsgService", "Fetching FCM registration token failed", task.exception)
                            return@OnCompleteListener
                        }

                        val token = task.result

                        val msg = getString(R.string.msg_token_fmt, token)
                        Log.d("FirebaseMsg", msg)
                        Firebase.sendRegistrationToServer(this,token)
                    })

                    // change page
                    // check if spotifyAccessToken is null, if so, redirect to SpotifyLoginActivity
                    if (loginRes.isNull("spotifyAccessToken")) {
                        val intent = Intent(this@LoginActivity, SpotifyConnectActivity::class.java)
                        startActivity(intent)
                    } else {
                        AuthManager.setSpotifyAuthed(this,true)
                        val intent = Intent(this@LoginActivity, PostsActivity::class.java)
                        startActivity(intent)
                    }
                },
                { error ->
                    val statusCode: Int = error.networkResponse.statusCode
                    if (statusCode == 404) {
                        // Username does not exist or password does not match
                        alert.setMessage("Incorrect username or password")
                        alert.create().show()
                    } else {
                        Log.e("Login", error.toString())
                        alert.setMessage("An unexpected error has occurred")
                        alert.create().show()
                    }
                }
            )
            queue.add(loginReq)
        }

        setContent {
            LoginScreen(handleLogin = ::handleLogin) { goBack() }
        }
    }
}

@Composable
fun LoginScreen(
    handleLogin: (LoginState) -> Unit,
    goBack: () -> Unit
) {
    var loginState by remember { mutableStateOf(LoginState()) }
    val loginEnabled = loginState.username.isNotEmpty() && loginState.password.isNotEmpty()
    val scrollState = rememberScrollState()

    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                goBack()
                            },
                            modifier = Modifier.width(90.dp),
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.back_arrow),
                                contentDescription = "Back",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.tunein_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(69.dp)
                        )
                        Spacer(modifier = Modifier.width(90.dp)) // use this to center the logo
                    }
                    Spacer(modifier = Modifier.height(64.dp))
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please Log into your existing account",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = loginState.username,
                        onValueChange = { loginState = loginState.copy(username = it)},
                        label = { Text(text = "Username", fontWeight = FontWeight.Light) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = loginState.password,
                        onValueChange = { loginState = loginState.copy(password = it)},
                        visualTransformation = PasswordVisualTransformation(),
                        label = { Text(text = "Password", fontWeight = FontWeight.Light) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { handleLogin(loginState) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loginEnabled
                    ) {
                        Text("Log In")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen({}) {}
}
