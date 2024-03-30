package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material.TextButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject


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

        setContent {
            PostsContent(
                user,
                handleClickFriends= { handleClickFriends() },
                handleClickSettings= ::handleClickSettings,
                handleClickComment= ::handleClickComment,
                feedViewModel = viewModel
            )
        }
    }
}

@Composable
fun PostItemGeneration(post: FeedPost, handleClickSettings: (user_id: String) -> Unit, handleClickComment: (postId: String) -> Unit) {
    // setup volley queue
    val queue = Volley.newRequestQueue(LocalContext.current)
    val ctx = LocalContext.current
    val likeCountURL = "${BuildConfig.BASE_URL}/likes/${post.id}"
    val commentCountURL = "${BuildConfig.BASE_URL}/comments_count/${post.id}"

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }
    var commentCount by remember { mutableIntStateOf(0) }

    // Getting like count for this post and get if user has liked this post
    val likeCountReq = object : JsonObjectRequest(
        Request.Method.GET, likeCountURL, null,
        { response ->
            likeCount = response.getInt("likes")
            isLiked = response.getBoolean("is_liked")
        },
        { error ->
            Log.e("PostsActivity", error.toString())

        }
    ) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] =
                "Bearer ${AuthManager.getAuthToken(ctx).toString()}"
            return headers
        }
    }
    // Getting comment count for this post
    val commentCountReq = object : JsonObjectRequest(
        Request.Method.GET, commentCountURL, null,
        { response ->
            commentCount = response.getInt("comments")
        },
        { error ->
            Log.e("PostsActivity", error.toString())

        }
    ) {
        @Throws(AuthFailureError::class)
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] =
                "Bearer ${AuthManager.getAuthToken(ctx).toString()}"
            return headers
        }
    }

    LaunchedEffect(post.id) {
        if (post.id != "") {
            queue.add(likeCountReq)
            queue.add(commentCountReq)
        }
    }

    // like handler function
    fun handleLike() {
        val method = Request.Method.POST
        likeCount = if (isLiked) likeCount - 1 else likeCount + 1
        val likeURL = if (isLiked) "${BuildConfig.BASE_URL}/unlike/${post.id}" else "${BuildConfig.BASE_URL}/like/${post.id}"
        isLiked = !isLiked
        val req = JSONObject()
        val likeReq = object : JsonObjectRequest(
            method, likeURL, req,
            { response ->
                likeCount = response.getInt("likes")
            },
            { error ->
                // revert the changes if there is an error
                isLiked = !isLiked
                Log.e("PostsActivity", error.toString())

            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer ${AuthManager.getAuthToken(ctx).toString()}"
                return headers
            }
        }
        queue.add(likeReq)
    }

    val uriHandler = LocalUriHandler.current

    Box(modifier = Modifier.padding(bottom = 8.dp, end = 8.dp).fillMaxWidth()) {
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
                Image(
                    painter = painterResource(id = R.drawable.spot),
                    contentDescription = "Spotify logo",
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            IconButton(
                onClick = { uriHandler.openUri(post.spotifyUrl) },
                modifier = Modifier.size(40.dp).padding(bottom = 8.dp, end = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.spot),
                    contentDescription = "Spotify logo",
                    modifier = Modifier
                        .size(24.dp)
                )
            }
            IconButton(
                onClick = { handleLike() },
                modifier = Modifier
                    .size(40.dp).padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.White
                )
            }
            Text(
                text = likeCount.toString(),
                style = TextStyle(
                    color = Color.LightGray,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            )
            IconButton(
                onClick = { handleClickComment(post.id) },
                modifier = Modifier.size(40.dp).padding(bottom = 8.dp)
            ) {
                androidx.compose.material.Icon(
                    painter = painterResource(id = R.drawable.comment),
                    contentDescription = "Comment",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
            Text(
                text = commentCount.toString(),
                style = TextStyle(
                    color = Color.LightGray,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            )
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

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        thread {
            viewModel.getPendingInvites(context)
        }
    }

    LaunchedEffect(returnedFeed) {
        val id = AuthManager.getUser(context).id
        feedViewModel.updateReturnedFeed(id)
    }

    // This was generated using GPT 3.5 OpenAI. (2023). ChatGPT (June 16 version) [Large language model]. https://chat.openai.com/chat
    suspend fun refreshFeed() {
        val id = AuthManager.getUser(context).id
        feedViewModel.updateReturnedFeed(id)
    }
    // End of GPT 3.5 Generation

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
                    TextButton(
                        // This was generated using GPT 3.5 OpenAI. (2023). ChatGPT (June 16 version) [Large language model]. https://chat.openai.com/chat
                        onClick = { coroutineScope.launch { refreshFeed() } }
                        // End of GPT 3.5 Generation
                    ) {
                        Text(
                            text = "TuneIn",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    IconButton(onClick = { handleClickSettings(user.id) }) {
                        Icon(Icons.Default.Face, contentDescription = "Settings")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(returnedFeed.posts) { post ->
                        PostItemGeneration(post, handleClickSettings, handleClickComment)
                    }
                }
            }
        }
    }

}
