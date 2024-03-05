package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.viewmodel.SearchResultsViewModel
import ca.uwaterloo.tunein.viewmodel.SearchUsers
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Dummy data for pending friend requests
val pendingFriendRequests = listOf(
    User(
        firstName = "DJ",
        lastName = "Khalid",
        username = "djkhalid"
    ),
    User(
        firstName = "The",
        lastName = "Weeknd",
        username = "theweeknd"
    ),
    User(
        firstName = "drake",
        lastName = "drake",
        username = "drake"
    ),
    User(
        firstName = "john",
        lastName = "doe",
        username = "johndoe123"
    ),
)

suspend fun handleAddFriend(user: User): Boolean = withContext(Dispatchers.IO) {
    val searchUrl = "${BuildConfig.BASE_URL}/friendship"
    val request = Request(
        url = searchUrl.toHttpUrl()
    )
    val response = OkHttpClient().newCall(request).execute()

    val json = response.body?.string()
    val j = Json{ ignoreUnknownKeys = true }
    j.decodeFromString<List<User>>(json!!)
    return@withContext true
}

class FriendsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val curUser = AuthManager.getUser(this)

        fun goBack() {
            val intent = Intent(this, PostsActivity::class.java)
            startActivity(intent)
        }

        fun handleAddFriend(user: User) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")

            val queue = Volley.newRequestQueue(this)
            val reqUrl = "${BuildConfig.BASE_URL}/friendships"

            val req = JSONObject()
            req.put("userIdRequesting", AuthManager.getUser(this).id)
            req.put("userIdReceiving", user.id)

            val loginReq = JsonObjectRequest(
                com.android.volley.Request.Method.POST, reqUrl, req,
                { _ ->
                    // show success toast
                    Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show()
                },
                { error ->
                    val statusCode: Int = error.networkResponse.statusCode
                    if (statusCode == 404) {
                        // Friend request already exists
                        alert.setMessage("You have already sent this person a friend request")
                        alert.create().show()
                    } else {
                        Log.e("FriendRequest", error.toString())
                        alert.setMessage("An unexpected error has occurred")
                        alert.create().show()
                    }
                }
            )
            queue.add(loginReq)
        }

        setContent {
            FriendsContent(
                user = curUser,
                { handleAddFriend(it) },
            ) { goBack() }
        }
    }
}

@Composable
fun FriendsContent(
    user: User,
    handleAddFriend: (user: User) -> Unit,
    viewModel: SearchResultsViewModel = viewModel(),
    goBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val searchUserState by viewModel.searchUsers.collectAsStateWithLifecycle()

    TuneInTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
                    }

                    Text(
                        text = "TuneIn",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(modifier = Modifier.width(40.0.dp))
                }
                SearchBar(
                    user,
                    searchUserState,
                    viewModel,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                ) {
                    if (searchUserState.searchQuery.text.isNotEmpty()) {
                        SearchFriends(
                            handleAddFriend,
                            searchUserState
                        )
                    } else  {
                        if (pendingFriendRequests.isNotEmpty()) {
                            PendingFriendRequests(pendingFriendRequests)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        // TODO: implement recommended friends (friends of friends)
                        // RecommendedFriends(recommendedFriends)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchFriends(
    handleAddFriend: (user: User) -> Unit,
    searchUserState: SearchUsers,
) {
    for (user in searchUserState.users) {
        SearchResultsRow(
            handleAddFriend,
            user
        )
    }
}

@Composable
fun SearchResultsRow(
    handleAddFriend: (user: User) -> Unit,
    user: User
) {
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Column(
                modifier = Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp)
            ){
                Image(
                    painter = painterResource(id = R.drawable.weeknd),
                    contentDescription = "weeknd art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .aspectRatio(1f / 1f)
                )
            }
            Column {
                Text(user.firstName)
                Text("@${user.username}", fontSize=12.sp, color = Color.LightGray)
            }
        }
        IconButton(onClick = { handleAddFriend(user) }) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Add friend",
                modifier = Modifier.size(18.dp),
                tint = androidx.compose.ui.graphics.Color.Green
            )
        }
    }
}

@Composable
fun PendingFriendRequests(requests: List<User>) {
    Column {
        Text(
            text = "Pending Friend Requests:",
        )
        Spacer(modifier = Modifier.height(8.dp))
        requests.forEach { user ->
            PendingFriendRow(user)
        }
    }
}

@Composable
fun PendingFriendRow(user: User) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ){
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Column(
                    modifier = Modifier.padding(0.dp, 0.dp, 20.dp, 0.dp)
                ){
                    Image(
                        painter = painterResource(id = R.drawable.weeknd),
                        contentDescription = "weeknd art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .aspectRatio(1f / 1f)
                    )
                }
                Column {
                    Text(user.firstName)
                    Text("@${user.username}", fontSize=12.sp, color = Color.LightGray)
                }
            }
            Row {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = androidx.compose.ui.graphics.Color.Red
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = androidx.compose.ui.graphics.Color.Green
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    user: User,
    searchUserState: SearchUsers,
    viewModel: SearchResultsViewModel
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = searchUserState.searchQuery,
        onValueChange = { viewModel.updateSearchUsers(user,  it) },
        label = { Text(text = "Add or search friends", fontWeight = FontWeight.Light) },
    )
}

@Preview
@Composable
fun PreviewFriendsContent() {
    val user = User("1", "jd123", "John", "Doe")
    FriendsContent(user, {}) {}
}