package ca.uwaterloo.tunein

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.components.ProfilePic
import ca.uwaterloo.tunein.data.Post
import ca.uwaterloo.tunein.messaging.Firebase
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.viewmodel.ProfileViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val showDialog = mutableStateOf(false)

class ProfileActivity : ComponentActivity() {

    private val viewModel by viewModels<ProfileViewModel>()

    override fun onStart() {
        super.onStart()
        val userId = this.intent.getStringExtra("user_profile").toString()
        viewModel.getProfile(userId)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = this.intent.getStringExtra("user_profile").toString()
        val user = AuthManager.getUser(this)

        fun goBack() {
            finish()
        }

        fun handleClickAccountSettings() {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        fun handleLogout() {
            AuthManager.setAuthToken(this, null)
            Firebase.clearRegistrationToken(this)
            startActivity(Intent(this, MainActivity::class.java))
        }

        fun onConfirmation() {
            showDialog.value = false
            startActivity(Intent(this, PostsActivity::class.java))
        }

        fun onDismissRequest() {
            showDialog.value = false
        }

        setContent {
            ProfileContent(
                selfProfile = userId == user.id,
                goBack={goBack()},
                handleClickAccountSettings={handleClickAccountSettings()},
                handleLogout = { handleLogout() },
                onConfirmation={ onConfirmation() },
                onDismissRequest={ onDismissRequest() },
                profileViewModel = viewModel
            )
        }
    }
}


@Composable
fun ProfileContent(
    selfProfile: Boolean,
    goBack: () -> Unit,
    handleClickAccountSettings: () -> Unit,
    handleLogout: () -> Unit,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val posts by profileViewModel.posts.collectAsStateWithLifecycle()

    TuneInTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
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
                    Column {
                        IconButton(onClick = { goBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back"
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text("Profile", textAlign= TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                    ){
                        Text(text = profile.firstName, fontSize = 32.sp)
                        Text(text = "@${profile.username}", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(34.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                        ){
                            Image(
                                painter = painterResource(id = R.drawable.spot),
                                contentDescription = "Spotify logo",
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Text(text = profile.spotifyName, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                        ){
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            val friendsText = if (profile.friendsNum == 1) "Friend" else "Friends"
                            Text(text = "${profile.friendsNum} $friendsText", fontSize=16.sp)
                        }
                    }
                    Column(
                        modifier = Modifier
                    ){
                        ProfilePic(
                            url = profile.profilePic,
                            modifier = Modifier
                                .size(156.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .aspectRatio(1f / 1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = Color.LightGray)
                )
                Spacer(modifier = Modifier.height(16.dp))
                posts.forEach{
                    PostHistory(
                        it,
                        profileViewModel::updateVisibility,
                        selfProfile
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (selfProfile) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { handleClickAccountSettings() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Account Settings",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { handleLogout() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Log Out",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDialog.value = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Delete Account",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("TuneIn Member Since ${profile.created}", fontSize=10.sp, color=Color.LightGray)
            }
        }
    }
    if(showDialog.value) {
        DialogWithImage(onDismissRequest, onConfirmation)
    }
}

@Composable
fun PostHistory(
    post: Post,
    updateVisibility: suspend (Post, Context) -> Unit,
    selfProfile: Boolean,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Text(text = post.caption, fontSize = 12.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
            ){
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .aspectRatio(1f / 1f)
                )
            }
            Column{
                Text(post.name)
                Text(post.artists, fontSize=12.sp, color = Color.LightGray)
                if (selfProfile) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Share with friends:")
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Switch (
                            modifier = Modifier.height(10.dp),
                            checked = post.visible,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    updateVisibility(post, context)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color = Color.MediumGray)
    )
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable
fun DialogWithImage(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.DarkGray,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Confirm that you would like to delete your account",
                    modifier = Modifier.padding(16.dp),
                    color=Color.LightGray
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss", color=Color.LightGreen)
                    }
                    TextButton(
                        onClick = { onConfirmation() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm", color=Color.LightGreen)
                    }
                }
            }
        }
    }
}
