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
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import ca.uwaterloo.tunein.auth.AuthManager

data class SignupState(
    var username: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var firstName: String = "",
    var lastName: String = "",
)

class SignupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        fun goBack() {
            super.finish()
        }

        fun handleSignup(signupState: SignupState) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")
            if (signupState.password != signupState.confirmPassword){
                alert.setMessage("Passwords do not match")
                alert.create().show()
                return
            } else if (signupState.password.length < 6) {
                alert.setMessage("Password must be at least 6 characters long")
                alert.create().show()
                return
            }

            val queue = Volley.newRequestQueue(this)
            val url = "${BuildConfig.BASE_URL}/user"

            val req = JSONObject()
            req.put("firstName", signupState.firstName)
            req.put("lastName", signupState.lastName)
            req.put("username", signupState.username)
            req.put("password", signupState.password)

            val createUserReq = JsonObjectRequest(Request.Method.POST, url, req,
                { _ ->
                    // persist logged in state
                    AuthManager.setLoggedIn(this,true)
                    AuthManager.setUsername(this, signupState.username)
                    // change page
                    val intent = Intent(this@SignupActivity, SpotifyConnectActivity::class.java)
                    startActivity(intent)
                },
                { error ->
                    Log.e("CreateUser", error.toString())
                    alert.setMessage("An unexpected error has occurred")
                    alert.create().show()
                }
            )

            val userExistsReq = JsonObjectRequest(Request.Method.GET, "${url}/${signupState.username}", null,
                { _ ->
                    alert.setMessage("Username already taken")
                    alert.create().show()
                },
                { error ->
                    val statusCode: Int = error.networkResponse.statusCode
                    if (statusCode == 404) {
                        // User does not exist, create new user
                        queue.add(createUserReq)
                    } else {
                        Log.e("CreateUser", error.toString())
                        alert.setMessage("An unexpected error has occurred")
                        alert.create().show()
                    }
                }
            )
            queue.add(userExistsReq)
        }

        setContent {
            SignupScreen(handleSignup = ::handleSignup) { goBack() }
        }
    }
}

@Composable
fun SignupScreen(
    handleSignup: (SignupState) -> Unit,
    goBack: () -> Unit
) {
    var signupState by remember { mutableStateOf(SignupState()) }
    val signupEnabled = signupState.username.isNotEmpty() &&
            signupState.password.isNotEmpty() &&
            signupState.confirmPassword.isNotEmpty() &&
            signupState.firstName.isNotEmpty() &&
            signupState.lastName.isNotEmpty()

    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF003847)
        ) {
            var scrollState = rememberScrollState()
            Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
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
                            text = "Welcome!",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                            value = signupState.firstName,
                            onValueChange = { signupState = signupState.copy(firstName = it) },
                            label = { Text(text = "First Name", color = Color.White, fontWeight = FontWeight.Light) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = signupState.lastName,
                            onValueChange = { signupState = signupState.copy(lastName = it) },
                            label = { Text(text = "Last Name", color = Color.White, fontWeight = FontWeight.Light) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = signupState.username,
                            onValueChange = { signupState = signupState.copy(username = it) },
                            label = { Text(text = "Username", color = Color.White, fontWeight = FontWeight.Light) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = signupState.password,
                            onValueChange = { signupState = signupState.copy(password = it) },
                            visualTransformation = PasswordVisualTransformation(),
                            label = { Text(text = "Password", color = Color.White, fontWeight = FontWeight.Light) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = signupState.confirmPassword,
                            onValueChange = { signupState = signupState.copy(confirmPassword = it) },
                            visualTransformation = PasswordVisualTransformation(),
                            label = { Text(text = "Confirm Password", color = Color.White, fontWeight = FontWeight.Light) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(
                        onClick = { handleSignup(signupState) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BC990)),
                        enabled = signupEnabled
                ) {
                    Text("Sign Up")
                }
            }
        }
    }
}

@Preview
@Composable
fun SignupScreenPreview() {
    SignupScreen({}) {}
}