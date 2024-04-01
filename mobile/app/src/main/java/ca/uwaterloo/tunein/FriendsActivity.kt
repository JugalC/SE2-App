package ca.uwaterloo.tunein

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.components.ProfilePic
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.viewmodel.FriendsViewModel
import ca.uwaterloo.tunein.viewmodel.SearchResults
import ca.uwaterloo.tunein.viewmodel.SearchResultsViewModel
import ca.uwaterloo.tunein.viewmodel.SearchUsers
import ca.uwaterloo.tunein.viewmodel.searchResultsToUser
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.concurrent.thread

class FriendsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this

        fun goBack() {
            finish()
        }

        val friendsViewModel = FriendsViewModel()

        fun handleAcceptInvite(user: User, accept: Boolean) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")

            val queue = Volley.newRequestQueue(this)
            val reqUrl = "${BuildConfig.BASE_URL}/friendship-request/${user.id}"

            val req = JSONObject()
            req.put("action", if (accept) "accept" else "reject")
            val acceptInviteReq = object : JsonObjectRequest(
                Method.PUT, reqUrl, req,
                { _ ->
                    if (accept) {
                        friendsViewModel.getPendingInvites(this)
                        friendsViewModel.getCurrentFriends(this)
                        alert.setMessage("Friend request accepted!").setTitle("Success")
                    } else {
                        friendsViewModel.removePendingInvite(user)
                        alert.setMessage("Friend request rejected!").setTitle("Success")
                    }
                    alert.create().show()
                },
                { error ->
                    Log.e("FriendRequest", "$error")
                    val statusCode: Int = error.networkResponse.statusCode
                    if (statusCode == 404) {
                        // Friend request already exists
                        alert.setMessage("Could not accept friend request")
                        alert.create().show()
                    } else {
                        Log.e("FriendRequest", "PUT Status ${statusCode}: $error")
                        alert.setMessage("An unexpected error has occurred")
                        alert.create().show()
                    }
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "Bearer ${AuthManager.getAuthToken(context).toString()}"
                    return headers
                }
            }
            queue.add(acceptInviteReq)
        }

        fun handleAddFriend(user: SearchResults) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")

            val queue = Volley.newRequestQueue(this)
            val reqUrl = "${BuildConfig.BASE_URL}/friendship-request/${user.id}"

            val req = JSONObject()
            val addFriendReq = object : JsonObjectRequest(Method.POST, reqUrl, req,
                { _ ->
                    // show success message
                    alert.setMessage("Friend request sent successfully").setTitle("Success")
                    alert.create().show()
                },
                { error ->
                    val statusCode: Int = error.networkResponse.statusCode
                    if (statusCode == 404) {
                        // Friend request already exists
                        alert.setMessage("You have already sent this person a friend request")
                        alert.create().show()
                    } else {
                        Log.e("FriendRequest", "POST Status ${statusCode}: $error")
                        alert.setMessage("An unexpected error has occurred")
                        alert.create().show()
                    }
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "Bearer ${AuthManager.getAuthToken(context).toString()}"
                    return headers
                }
            }

            queue.add(addFriendReq)
        }

        fun handleRemoveFriend(user: User) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")

            val queue = Volley.newRequestQueue(this)
            val reqUrl = "${BuildConfig.BASE_URL}/friendship/${user.id}"

            val req = JSONObject()
            val removeFriendReq = object : JsonObjectRequest(Method.POST, reqUrl, req,
                { _ ->
                    // show success message
                    friendsViewModel.removeFriend(user)
                    alert.setMessage("Friend successfully removed").setTitle("Success")
                    alert.create().show()
                },
                { error ->
                    val statusCode: Int = error.networkResponse.statusCode
                    Log.e("RemoveFriend", "DELETE Status ${statusCode}: $error")
                    alert.setMessage("An unexpected error has occurred")
                    alert.create().show()
                }
            ) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] =
                        "Bearer ${AuthManager.getAuthToken(context).toString()}"
                    return headers
                }
            }

            queue.add(removeFriendReq)
        }

        setContent {
            FriendsContent(
                user = AuthManager.getUser(this),
                { handleRemoveFriend(it) },
                { handleAddFriend(it) },
                ::handleAcceptInvite,
                friendsViewModel,
            ) { goBack() }
        }
    }
}

@Composable
fun FriendsContent(
    user: User,
    handleRemoveFriend: (user: User) -> Unit,
    handleAddFriend: (user: SearchResults) -> Unit,
    handleAcceptInvite: (user: User, accept: Boolean) -> Unit,
    friendsViewModel: FriendsViewModel = viewModel(),
    searchResultsViewModel: SearchResultsViewModel = viewModel(),
    goBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val searchUserState by searchResultsViewModel.searchUsers.collectAsStateWithLifecycle()
    val pendingInvites by friendsViewModel.pendingInvites.collectAsStateWithLifecycle()
    val currentFriends by friendsViewModel.friends.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        thread {
            friendsViewModel.getPendingInvites(context)
        }
        thread {
            friendsViewModel.getCurrentFriends(context)
        }
    }

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text("Friends", textAlign= TextAlign.Center)
                    }
                    Spacer(Modifier.weight(1f))
                    Column {
                        IconButton(onClick = { goBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Go Back"
                            )
                        }
                    }
                }
                SearchBar(
                    user,
                    searchUserState,
                    searchResultsViewModel,
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
                            handleRemoveFriend,
                            handleAcceptInvite,
                            searchUserState
                        )
                    } else {
                        if (pendingInvites.users.isNotEmpty()) {
                            PendingFriendRequests(pendingInvites.users, handleAcceptInvite)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        MyFriends(currentFriends.users, handleRemoveFriend)
                    }
                }
            }
        }
    }
}

@Composable
fun MyFriends(friends: List<User>, handleRemoveFriend: (user: User) -> Unit) {
    Column {
        Text(
            text = "My friends (${friends.size}):",
        )
        Spacer(modifier = Modifier.height(8.dp))
        friends.forEach { user ->
            FriendRow(user, handleRemoveFriend)
        }
    }
}

@Composable
fun SearchFriends(
    handleAddFriend: (user: SearchResults) -> Unit,
    handleRemoveFriend: (user: User) -> Unit,
    handleAcceptInvite: (user: User, accept: Boolean) -> Unit,
    searchUserState: SearchUsers,
) {
    if (searchUserState.friends.isNotEmpty()) {
        Text(
            text = "My friends (${searchUserState.friends.size}):",
        )
        Spacer(modifier = Modifier.height(8.dp))
        for (user in searchUserState.friends) {
            FriendRow(
                searchResultsToUser(user),
                handleRemoveFriend
            )
        }
    }
    if (searchUserState.users.isNotEmpty()) {
        Text(
            text = "Users (${searchUserState.users.size}):",
        )
        Spacer(modifier = Modifier.height(8.dp))
        for (user in searchUserState.users) {
            SearchResultsRow(
                handleAddFriend,
                user
            )
        }
    }
    if (searchUserState.friendRequests.isNotEmpty()) {
        Text(
            text = "Friend Requests (${searchUserState.friendRequests.size}):",
        )
        Spacer(modifier = Modifier.height(8.dp))
        for (user in searchUserState.friendRequests) {
            PendingFriendRow(
                searchResultsToUser(user),
                handleAcceptInvite,
            )
        }
    }
}

@Composable
fun SearchResultsRow(
    handleAddFriend: (user: SearchResults) -> Unit,
    user: SearchResults
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
            ) {
                ProfilePic(
                    url = user.profilePicture,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .aspectRatio(1f / 1f)
                )
            }
            Column {
                Text("${user.firstName} ${user.lastName}")
                Text("@${user.username}", fontSize = 12.sp, color = Color.LightGray)
            }
        }
        IconButton(onClick = { handleAddFriend(user) }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add friend",
                modifier = Modifier.size(18.dp),
                tint = androidx.compose.ui.graphics.Color.Green
            )
        }
    }
}

@Composable
fun PendingFriendRequests(requests: List<User>, handleAcceptInvite: (user: User, accept: Boolean) -> Unit) {
    Column {
        Text(
            text = "Pending Friend Requests (${requests.size}):",
        )
        Spacer(modifier = Modifier.height(8.dp))
        requests.forEach { user ->
            PendingFriendRow(user, handleAcceptInvite)
        }
    }
}

@Composable
fun FriendRow(user: User, handleRemoveFriend: (user: User) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
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
                ) {
                    ProfilePic(
                        url = user.profilePicture,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .aspectRatio(1f / 1f)
                    )
                }
                Column {
                    Text(user.firstName)
                    Text("@${user.username}", fontSize = 12.sp, color = Color.LightGray)
                }
            }
            Row {
                IconButton(
                    onClick = { handleRemoveFriend(user) },
                    modifier = Modifier.size(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun PendingFriendRow(user: User, handleAcceptInvite: (user: User, accept: Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
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
                ) {
                    ProfilePic(
                        url = user.profilePicture,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .aspectRatio(1f / 1f)
                    )
                }
                Column {
                    Text(user.firstName)
                    Text("@${user.username}", fontSize = 12.sp, color = Color.LightGray)
                }
            }
            Row {
                IconButton(
                    onClick = { handleAcceptInvite(user, false) },
                    modifier = Modifier.size(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = androidx.compose.ui.graphics.Color.Red
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { handleAcceptInvite(user, true) },
                    modifier = Modifier.size(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.Green
                    )
                }
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
        onValueChange = { viewModel.updateSearchUsers(user, it) },
        label = { Text(text = "Add or search friends", fontWeight = FontWeight.Light) },
    )
}

@Preview
@Composable
fun PreviewFriendsContent() {
    val user = User("1", "jd123", "John", "Doe")
    FriendsContent(user, {}, {}, {_, _ -> }) {}
}