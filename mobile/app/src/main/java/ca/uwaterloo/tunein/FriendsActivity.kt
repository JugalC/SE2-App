package ca.uwaterloo.tunein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ca.uwaterloo.tunein.ui.theme.TuneInTheme

class FriendsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FriendsContent()
        }
    }
}

@Composable
fun FriendsContent() {
    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF003847)
        ) {
            Text(text = "Friends")
        }
    }
}

@Preview
@Composable
fun PreviewFriendsContent() {
    ProfileContent()
}