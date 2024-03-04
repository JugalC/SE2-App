package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
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
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.viewmodel.SearchResultsViewModel
import ca.uwaterloo.tunein.viewmodel.SearchUsers

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

class FriendsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun goBack() {
            val intent = Intent(this, PostsActivity::class.java)
            startActivity(intent)
        }

        setContent {
            FriendsContent { goBack() }
        }
    }
}

@Composable
fun FriendsContent(
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
                    Spacer(modifier = Modifier.width(40.0.dp))
                    Text(
                        text = "TuneIn.",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    IconButton(onClick = { goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Go Back")
                    }
                }
                SearchBar(
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
    searchUserState: SearchUsers,
) {
    for (user in searchUserState.users) {
        SearchResultsRow(
            user
        )
    }
}

@Composable
fun SearchResultsRow(user: User) {
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
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = androidx.compose.ui.graphics.Color.Green
        )
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
    searchUserState: SearchUsers,
    viewModel: SearchResultsViewModel
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = searchUserState.searchQuery,
        onValueChange = { viewModel.updateSearchUsers(it) },
        label = { Text(text = "Add or search friends", fontWeight = FontWeight.Light) },
    )
}

@Preview
@Composable
fun PreviewFriendsContent() {
    FriendsContent {}
}