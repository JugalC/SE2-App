package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.viewmodel.FriendsViewModel
import kotlin.concurrent.thread

data class Post(val id: Int, val content: String, val author: String, val imageResId: Int, val profilePhotoResId: Int, val username: String)
val samplePosts = listOf(
    Post(1, "Passport Bros (with J.Cole)", "Bas, J. Cole", R.drawable.jcole_passport, R.drawable.stock_profile, "JohnDoe123"),
    Post(2, "Blinding Lights", "The Weeknd", R.drawable.the_weeknd_blinding,R.drawable.stock_profile, "JaneDoe321" ),
    Post(3, "Marvins Room", "Drake", R.drawable.drake_marvins,R.drawable.stock_profile, "JohnSmith123")
)

class PostsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun handleClickFriends() {
            val intent = Intent(this, FriendsActivity::class.java)
            startActivity(intent)
        }

        fun handleClickSettings() {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        setContent {
            PostsContent(
                handleClickFriends= { handleClickFriends() },
                handleClickSettings= { handleClickSettings() },
            )
        }
    }
}



@Composable
fun PostItem(post: Post, handleClickSettings: () -> Unit) {
    var isLiked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(bottom = 16.dp, end = 16.dp).fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp,
            backgroundColor = Color(0xFF1E1E1E),
        ) {
            Column {
                Row (modifier = Modifier.clickable { handleClickSettings() }) 
                {
                    Image(
                        painter = painterResource(id = post.profilePhotoResId),
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .align(Alignment.Top)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(text = post.username, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row{
                    Image(
                        painter = painterResource(id = post.imageResId),
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = post.content, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = post.author, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        IconButton(
            onClick = { isLiked = !isLiked },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(48.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) Color.Red else Color.White
            )
        }
    }
}



@Composable
fun PostsContent(
    handleClickFriends: () -> Unit,
    handleClickSettings: () -> Unit,
    viewModel: FriendsViewModel = viewModel()
) {
    val context = LocalContext.current
    val user = AuthManager.getUser(context)
    val pendingInvites by viewModel.pendingInvites.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        thread {
            viewModel.getPendingInvites(user = user)
        }
    }

    TuneInTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
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
                    if (pendingInvites.users.isNotEmpty()) {
                        IconButton(onClick = { handleClickFriends() }) {
                            Icon(Icons.Outlined.Person, contentDescription = "Friends")
                        }
                    } else {
                        IconButton(onClick = { handleClickFriends() }) {
                            Icon(Icons.Default.Person, contentDescription = "Friends")
                        }
                    }
                    Text(
                        text = "TuneIn.",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    IconButton(onClick = { handleClickSettings() }) {
                        Icon(Icons.Default.Face, contentDescription = "Settings")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(samplePosts) { post ->
                        Spacer(modifier = Modifier.height(25.dp))
                        PostItem(post) {
                            handleClickSettings()
                        }
                    }
                }
            }
        }
    }

}

@Preview
@Composable
fun PostsPreview() {
    PostsContent({}, {})
}