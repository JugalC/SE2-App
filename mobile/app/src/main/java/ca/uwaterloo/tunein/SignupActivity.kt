package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
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

data class SignupState(
    var username: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var firstName: String = "",
    var lastName: String = "",
)


class SignupActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        fun goBack() {
            super.finish()
        }

        fun handleSignup() {
            // TODO: verify credentials against backend
            val signupSuccessful = true

            // persist logged in state
            if (signupSuccessful) {
                authViewModel.setLoggedIn(true)
            }

            // change page
            val intent = Intent(this@SignupActivity, PostsActivity::class.java)
            startActivity(intent)
        }

        setContent {
            SignupScreen(handleSignup = {handleSignup()}) { goBack() }
        }
    }
}

@Composable
fun SignupScreen(
    handleSignup: () -> Unit,
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
                        onClick = { handleSignup() },
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