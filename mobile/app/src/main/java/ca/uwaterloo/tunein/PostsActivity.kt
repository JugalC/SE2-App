package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.viewmodel.FeedViewModel
import kotlin.concurrent.thread
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.viewmodel.FriendsViewModel
import ca.uwaterloo.tunein.data.FeedPost
import coil.compose.AsyncImage


class PostsActivity : ComponentActivity() {
    private val viewModel by viewModels<FeedViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = AuthManager.getUser(this)

        fun handleClickFriends() {
            val intent = Intent(this, FriendsActivity::class.java)
            startActivity(intent)
        }

        fun handleClickSettings(user_id: String) {
            val intent = Intent(this, ProfileActivity::class.java)
            println("Adding $user_id")
            intent.putExtra("user_profile", user_id)
            startActivity(intent)
        }

        fun handleClickComment(postId: String) {
            val intent = Intent(this, CommentsActivity::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }

        fun handleClickComment(postId: String) {
            val intent = Intent(this, CommentsActivity::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }

        setContent {
            PostsContent(
                user,
                handleClickFriends= { handleClickFriends() },
                handleClickSettings= ::handleClickSettings,
                handleClickComment = ::handleClickComment,
                feedViewModel = viewModel
            )
        }
    }
}

@Composable
//fun PostItemGeneration(post: SinglePost, handleClickSettings: (user_id: String) -> Unit, handleClickComment: (postId: String) -> Unit) {
fun PostItemGeneration(post: FeedPost, handleClickSettings: (user_id: String) -> Unit) {
//fun PostItemGeneration(post: SinglePost, handleClickSettings: (user_id: String) -> Unit, handleClickComment: (postId: String) -> Unit) {
    var isLiked by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Box(modifier = Modifier.padding(bottom = 16.dp, end = 16.dp).fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp,
            backgroundColor = Color(0xFF1E1E1E),
        ) {
            Column {
                Row (modifier = Modifier.clickable { handleClickSettings(post.userId) })
                {
                    AsyncImage(
                        model = post.profilePicture,
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
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
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = post.name, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(text = post.artists, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart)
        ){
            IconButton(
                onClick = { uriHandler.openUri(post.spotifyUrl) },
                modifier = Modifier.size(48.dp)
            ) {
                androidx.compose.material.Icon(
                    painter = painterResource(id = R.drawable.green_play_button),
                    contentDescription = "Comment",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {

            IconButton(
                onClick = { isLiked = !isLiked },
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.White
                )
            }
            IconButton(
                onClick = { /* TODO */ },
//                onClick = { handleClickComment(post.id) },
                modifier = Modifier.size(48.dp)
            ) {
                androidx.compose.material.Icon(
                    painter = painterResource(id = R.drawable.comment),
                    contentDescription = "Comment",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }
    }
}




@Composable
fun PostsContent(
    user: User,
    handleClickFriends: () -> Unit,
    handleClickSettings: (user_id: String) -> Unit,
    handleClickComment: (postId: String) -> Unit,
    viewModel: FriendsViewModel = viewModel(),
    feedViewModel: FeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val pendingInvites by viewModel.pendingInvites.collectAsStateWithLifecycle()

    val returnedFeed by remember { feedViewModel.returnedFeed }


    LaunchedEffect(Unit) {
        thread {
            viewModel.getPendingInvites(context)
        }
    }

    LaunchedEffect(returnedFeed) {
        feedViewModel.updateReturnedFeed(user.id)
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
                        text = "TuneIn",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    IconButton(onClick = { handleClickSettings(user.id) }) {
                        Icon(Icons.Default.Face, contentDescription = "Settings")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(returnedFeed.posts) { post ->
//                        PostItemGeneration(
//                            post = post,
//                            handleClickSettings = { userId -> handleClickSettings(userId) },
//                            handleClickComment = { postId -> handleClickComment(postId) }
//                        )
                        PostItemGeneration(post) {
                            handleClickSettings(post.userId)
                        }
//                        PostItemGeneration(
//                            post = post,
//                            handleClickSettings = { userId -> handleClickSettings(userId) },
//                            handleClickComment = { postId -> handleClickComment(postId) }
//                        )
                    }
                }
            }
        }
    }

}
