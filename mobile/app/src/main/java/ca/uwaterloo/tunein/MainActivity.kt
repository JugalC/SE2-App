package ca.uwaterloo.tunein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fun handleSignUp() {
            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
        setContent {
            MainScreen { handleSignUp() }

        }
    }
}

@Composable
fun MainScreen(handleSignUp: () -> Unit) {
    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF003847)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth())
                Image(
                    painter = painterResource(id = R.drawable.logo_fill),
                    contentDescription = "Content"
                )
                Spacer(modifier = Modifier.height(192.dp))
                Button(
                    onClick = {/*TODO*/ },
                    colors= ButtonDefaults.buttonColors(containerColor = Color(0xFF00FC64), contentColor = Color.Black),
                    modifier = Modifier.width(192.dp)
                ) {
                    Text(text = "Log In")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        handleSignUp()
                    },
                    colors= ButtonDefaults.buttonColors(containerColor = Color(0xFF00FC64), contentColor = Color.Black),
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
    MainScreen({})
}