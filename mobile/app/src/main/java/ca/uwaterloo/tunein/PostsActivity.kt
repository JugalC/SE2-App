package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.ui.theme.TuneInTheme

class PostsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun handleLogout() {
            AuthManager.setLoggedIn(this,false)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        setContent {
            PostsContent { handleLogout() }
        }
    }
}

@Composable
fun PostsContent(handleLogout: () -> Unit) {
    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Posts",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { handleLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BC990))
                ) {
                    Text(
                        text = "Log Out"
                    )
                }
            }
        }
    }

}

@Preview
@Composable
fun PostsPreview() {
    PostsContent {}
}