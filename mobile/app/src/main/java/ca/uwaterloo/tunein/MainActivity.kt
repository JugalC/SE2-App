package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.ui.theme.TuneInTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthManager.isLoggedIn(this)) {
            if (AuthManager.isSpotifyAuthed(this)) {
                val intent = Intent(this@MainActivity, PostsActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@MainActivity, SpotifyConnectActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        fun handleSignup() {
            val intent = Intent(this@MainActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        fun handleLogin() {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        setContent {
            MainScreen ({ handleSignup() }, { handleLogin() })
        }
    }
}

@Composable
fun MainScreen(handleSignup: () -> Unit, handleLogin: () -> Unit) {
    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth())
                Image(
                    painter = painterResource(id = R.drawable.logo_fill),
                    contentDescription = "Content",
                    modifier = Modifier.padding(36.dp)
                )
                Spacer(modifier = Modifier.height(192.dp))
                Button(
                    onClick = {
                        handleLogin()
                    },
                    modifier = Modifier.width(192.dp)
                ) {
                    Text(text = "Log In")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        handleSignup()
                    },
                    modifier = Modifier.width(192.dp)
                ) {
                    Text(text = "Sign Up")
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen({},{})
}