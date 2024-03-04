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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.ui.theme.TuneInTheme



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
fun FriendsContent(goBack: () -> Unit) {
    val searchTextState = remember { mutableStateOf(String) }
    val scrollState = rememberScrollState()

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
                SearchBar()
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                ) {
                    if (pendingFriendRequests.isNotEmpty()) {
                        PendingFriendRequests(pendingFriendRequests)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

//                    TODO: implement recommended friends (friends of friends)
//                    RecommendedFriends(recommendedFriends)
                }
            }
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
            horizontalArrangement = Arrangement.SpaceBetween
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
                Text(text="add")
            }
        }
    }
}

@Composable
fun SearchBar() {
    val searchTextState = remember { mutableStateOf(TextFieldValue()) }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = searchTextState.value,
        onValueChange = { searchTextState.value = it},
        label = { Text(text = "Add or search friends", fontWeight = FontWeight.Light) },
    )
}

@Preview
@Composable
fun PreviewFriendsContent() {
    FriendsContent {}
}