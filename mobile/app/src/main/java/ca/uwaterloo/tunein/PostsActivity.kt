package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

        fun handleClickFriends() {
            val intent = Intent(this, FriendsActivity::class.java)
            startActivity(intent)
        }

        fun handleClickSettings() {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        setContent {
            PostsContent(handleClickFriends= { handleClickFriends() }, handleClickSettings= { handleClickSettings() }) { handleLogout() }
        }
    }
}





@Composable
fun PostsContent(handleClickFriends: () -> Unit, handleClickSettings: () -> Unit, handleLogout: () -> Unit) {
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
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { handleClickFriends() }) {
                        Icon(Icons.Default.Person, contentDescription = "Friends", tint = Color.White)
                    }
                    Text(
                        text = "TuneIn.",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    IconButton(onClick = { handleClickSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
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
    PostsContent({}, {}) {}
}