package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import ca.uwaterloo.tunein.ui.viewmodel.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.ui.viewmodel.AuthViewModel

class LogInActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        fun goBack() {
            super.finish()
        }

        fun handleLogin() {
            // TODO: verify credentials against backend
            val loginSuccessful = true

            // persist logged in state
            if (loginSuccessful) {
                authViewModel.setLoggedIn(true)
            }

            // change page
            val intent = Intent(this@LogInActivity, PostsActivity::class.java)
            startActivity(intent)
        }
        setContent {
            LogInStateComposable(handleLogin = {handleLogin()}) { goBack() }
        }
    }
}

@Composable
fun LogInStateComposable(viewModel: LoginViewModel = viewModel(), handleLogin: () -> Unit, goBack: () -> Unit) {
    val loginState by viewModel.uiState.collectAsState()

    LogInScreen(
        email = loginState.email,
        password = loginState.password,
        setEmail = { viewModel.setEmail(it) },
        setPassword = { viewModel.setPassword(it) },
        handleLogin = { handleLogin() }
    ) {
        goBack()
    }
}

@Composable
fun LogInScreen(
    email: String,
    password: String,
    setEmail: (newEmail: String) -> Unit,
    setPassword: (newPassword: String) -> Unit,
    handleLogin: () -> Unit,
    goBack: () -> Unit
) {
    var loginEnabled = email.length > 0 && password.length > 0

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
                            value = email,
                            onValueChange = {setEmail(it)},
                            label = { Text(text = "Email", fontWeight = FontWeight.Light) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                            value = password,
                            visualTransformation = PasswordVisualTransformation(),
                            onValueChange = {setPassword(it)},
                            label = { Text(text = "Password", fontWeight = FontWeight.Light) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { handleLogin() },
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
fun LogInScreenPreview() {
    LogInScreen("test", "password", {}, {}, {}) {}
}
