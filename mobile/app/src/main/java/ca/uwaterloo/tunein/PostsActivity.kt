package ca.uwaterloo.tunein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import ca.uwaterloo.tunein.data.FeedPost
import ca.uwaterloo.tunein.data.Post
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.viewmodel.FeedViewModel
import ca.uwaterloo.tunein.viewmodel.FriendsViewModel
import coil.compose.AsyncImage
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class PostsActivity : ComponentActivity() {
    private val feedViewModel by viewModels<FeedViewModel>()
    private val friendsViewModel by viewModels<FriendsViewModel>()
    override fun onStart() {
        super.onStart()
        val user = AuthManager.getUser(this)
        feedViewModel.shouldShowPostBanner(this)
        feedViewModel.updateReturnedFeed(user.id)
        friendsViewModel.getPendingInvites(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun handleClickFriends() {
            val intent = Intent(this, FriendsActivity::class.java)
            startActivity(intent)
        }

        fun handleClickProfile(userId: String) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_profile", userId)
            startActivity(intent)
        }

        fun handleClickComment(postId: String) {
            val intent = Intent(this, CommentsActivity::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }

        setContent {
            PostsContent(
                handleClickFriends= { handleClickFriends() },
                handleClickProfile= ::handleClickProfile,
                handleClickComment= ::handleClickComment,
                feedViewModel = feedViewModel
            )
        }
    }
}

@Composable
fun NoPostsFound() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "No Posts Found", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Add friends or wait until the next post time", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PostItemGeneration(post: FeedPost, handleClickProfile: (userId: String) -> Unit,  handleClickComment: (postId: String) -> Unit) {
    // setup volley queue
    val queue = Volley.newRequestQueue(LocalContext.current)
    val ctx = LocalContext.current
    val likeCountURL = "${BuildConfig.BASE_URL}/likes/${post.id}"
    val commentCountURL = "${BuildConfig.BASE_URL}/comments_count/${post.id}"

    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }
    var commentCount by remember { mutableIntStateOf(0) }

    val uriHandler = LocalUriHandler.current

    // Getting like count for this post and get if user has liked this post
    val likeCountReq = object : JsonObjectRequest(
        Method.GET, likeCountURL, null,
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
        Method.GET, commentCountURL, null,
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

    Box(modifier = Modifier
        .padding(bottom = 8.dp, end = 8.dp)
        .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp,
            backgroundColor = Color(0xFF1E1E1E),
        ) {
            Column {
                Row (modifier = Modifier.clickable { handleClickProfile(post.userId) })
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
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            IconButton(
                onClick = { uriHandler.openUri(post.spotifyUrl) },
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 8.dp, end = 8.dp)
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
                    .size(40.dp)
                    .padding(bottom = 8.dp)
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
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 8.dp)
            ) {
                androidx.compose.material.Icon(
                    painter = painterResource(id = R.drawable.comment),
                    contentDescription = "Comment",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
            Text(
                text = post.commentsNum.toString(),
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
fun PostSongBanner(
    post: Post,
    feedViewModel: FeedViewModel,
    context: Context,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(BorderStroke(2.dp, Color.White)),
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Do you want to post a song today? You were listening to ${post.name} by ${post.artists}",
                modifier = Modifier.padding(16.dp),
            )
        }
        Row {
            IconButton(onClick = { feedViewModel.updatePostVisibility(context, true) }) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Post Song",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Green
                )
            }
            IconButton(onClick = { feedViewModel.updatePostVisibility(context, false) }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Do Not Post Song",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Red
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PostsContent(
    handleClickFriends: () -> Unit,
    handleClickProfile: (userId: String) -> Unit,
    handleClickComment: (postId: String) -> Unit,
    friendsViewModel: FriendsViewModel = viewModel(),
    feedViewModel: FeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val pendingInvites by friendsViewModel.pendingInvites.collectAsStateWithLifecycle()
    val feed by feedViewModel.feed.collectAsStateWithLifecycle()
    val refreshing by feedViewModel.isRefreshing.collectAsStateWithLifecycle()
    val showBanner by feedViewModel.showBanner.collectAsStateWithLifecycle()
    val mostRecentPost by feedViewModel.mostRecentPost.collectAsStateWithLifecycle()

    fun refreshFeed() {
        val user = AuthManager.getUser(context)
        feedViewModel.updateReturnedFeed(user.id)
    }

    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = { refreshFeed() })

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
                    IconButton(onClick = { handleClickProfile(AuthManager.getUser(context).id) }) {
                    Icon(Icons.Default.Face, contentDescription = "Profile")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .pullRefresh(pullRefreshState)
                ) {
                    if (feed.posts.isNotEmpty()) {
                        LazyColumn(modifier = Modifier
                            .fillMaxWidth()
                        ) {
                            if (showBanner) {
                                item {
                                    PostSongBanner(
                                        mostRecentPost,
                                        feedViewModel,
                                        context
                                    )
                                }
                            }
                            items(feed.posts) { post ->
                                PostItemGeneration(post, handleClickProfile, handleClickComment)
                            }
                        }
                    }
                    else {
                        NoPostsFound()
                    }

                    PullRefreshIndicator(
                        refreshing = refreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
        }
    }
}
