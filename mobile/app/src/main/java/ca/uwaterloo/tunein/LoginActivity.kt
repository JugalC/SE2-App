package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.ui.viewmodel.AuthViewModel
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

data class LoginState(
    var username: String = "",
    var password: String = "",
)

class LoginActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        fun goBack() {
            super.finish()
        }

        fun handleLogin(loginState: LoginState) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")

            val queue = Volley.newRequestQueue(this)
            val url = "${BuildConfig.BASE_URL}/login/${loginState.username}"

            val req = JSONObject()
            req.put("password", loginState.password)

            val loginReq = JsonObjectRequest(
                Request.Method.POST, url, req,
                { _ ->
                    // persist logged in state
                    authViewModel.setLoggedIn(true)
                    // change page
                    val intent = Intent(this@LoginActivity, PostsActivity::class.java)
                    startActivity(intent)
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

    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF003847)
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
                            color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please Log into your existing account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BC990)),
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
