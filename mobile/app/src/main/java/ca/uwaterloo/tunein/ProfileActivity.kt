package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.messaging.Firebase
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.ui.theme.TuneInTheme

private val showDialog = mutableStateOf(false)

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = AuthManager.getUser(this)

        fun goBack() {
            val intent = Intent(this, PostsActivity::class.java)
            startActivity(intent)
        }

        fun handleClickAccountSettings() {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        fun handleLogout() {
            AuthManager.setAuthToken(this, null)
            Firebase.clearRegistrationToken(this)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        fun onConfirmation() {
            val intent = Intent(this, PostsActivity::class.java)
            showDialog.value = false
            startActivity(intent)
        }

        fun onDismissRequest() {
            showDialog.value = false
        }

        setContent {
            ProfileContent(
                user,
                goBack={goBack()},
                handleClickAccountSettings={handleClickAccountSettings()},
                handleLogout = { handleLogout() },
                onConfirmation={onConfirmation()},
                onDismissRequest={onDismissRequest()}
            )
        }
    }
}


@Composable
fun ProfileContent(user: User,
                   goBack: () -> Unit,
                   handleClickAccountSettings: () -> Unit,
                   handleLogout: () -> Unit,
                   onConfirmation: () -> Unit,
                   onDismissRequest: () -> Unit) {
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
                    Column() {
                        IconButton(onClick = { goBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back"
                            )
                        }
                    }
                    Column(modifier = Modifier
                        .fillMaxWidth(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally
                        ){Text("Profile", textAlign= TextAlign.Center)}
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)

                    ){
                        Text(text = "${user.firstName} ${user.lastName}", fontSize=32.sp)
                        Text(text = "@${user.username}", fontSize=16.sp)
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
                            Text(text = " jd_spot", fontSize=16.sp)

                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                        ){
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(text = " 12 Friends", fontSize=16.sp)
                        }

                    }
                    Column(
                        modifier = Modifier

                    ){
                        ProfilePicture()
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
                PreviousPosts()

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
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
                        Text(text = "Account Settings")
                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
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
                    Text(text = "Log Out")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
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
                    Text(text = "Delete Account")
                }


                Spacer(modifier = Modifier.height(10.dp))
                Text("TuneIn Member Since January 1, 2024", fontSize=10.sp, color=Color.LightGray)

            }

        }
    }
    if(showDialog.value) {
        DialogWithImage(onDismissRequest, onConfirmation)
    }
}

@Composable
fun ProfileOption(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}

@Composable
fun ProfilePicture() {
    Image(
        painter = painterResource(id = R.drawable.profile_pic),
        contentDescription = "weeknd art",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(156.dp)
            .clip(RoundedCornerShape(16.dp))
            .aspectRatio(1f / 1f)


    )
}

@Composable
fun PreviousPosts() {
    Column(modifier = Modifier
        .fillMaxWidth()){
        Text(text = "Today", fontSize = 12.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(8.dp))
        Row() {
            Column(
                modifier = Modifier
                .fillMaxWidth(0.3f)
            ){
                Image(
                    painter = painterResource(id = R.drawable.weeknd),
                    contentDescription = "weeknd art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .aspectRatio(1f / 1f)


                )
            }
            Column(
                modifier = Modifier

            ){
                Text("After Hours")
                Text("The Weeknd", fontSize=12.sp, color = Color.LightGray)
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
//    Column(modifier = Modifier
//        .fillMaxWidth()){
//        Text(text = "Yesterday", fontSize = 12.sp, color = Color.LightGray)
//        Spacer(modifier = Modifier.height(8.dp))
//        Row() {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth(0.3f)
//            ){
//                Image(
//                    painter = painterResource(id = R.drawable.trilog),
//                    contentDescription = "weeknd art",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .size(96.dp)
//                        .clip(RoundedCornerShape(16.dp))
//                        .aspectRatio(1f / 1f)
//
//
//                )
//            }
//            Column(
//                modifier = Modifier
//
//            ){
//                Text("Wicked Games")
//                Text("The Weeknd", fontSize=12.sp, color = Color.LightGray)
//            }
//
//        }
//    }
//    Spacer(modifier = Modifier.height(8.dp))
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(1.dp)
//            .background(color = Color.MediumGray)
//    )
//    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier
        .fillMaxWidth()){
        Text(text = "2 Days Ago", fontSize = 12.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(8.dp))
        Row() {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
            ){
                Image(
                    painter = painterResource(id = R.drawable.starboy),
                    contentDescription = "weeknd art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .aspectRatio(1f / 1f)


                )
            }
            Column(
                modifier = Modifier

            ){
                Text("Die For You")
                Text("The Weeknd", fontSize=12.sp, color = Color.LightGray)
            }

        }
    }

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

@Preview
@Composable
fun PreviewProfileContent() {
    val user = User("JohnDoe123", "John", "Doe")
    ProfileContent(user, {}, {}, {}, {}) { }
}
