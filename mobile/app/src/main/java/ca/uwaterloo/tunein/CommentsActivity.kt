package ca.uwaterloo.tunein

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.Comment
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.viewmodel.CommentsViewModel
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject

class CommentsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun goBack() {
            finish()
        }

//        // create volley queue
//        val queue = Volley.newRequestQueue(this)
//        // url for updating username
//        val usernameURL = "${BuildConfig.BASE_URL}/user/update_user"
//        // url for updating password
//        val passwordURL = "${BuildConfig.BASE_URL}/user/update_password"
//        // url for updating photo
//        val photoURL = "${BuildConfig.BASE_URL}/user/update_profile_picture"
//        // url for reauthorizing spotify
//        // val spotifyURL = "${BuildConfig.BASE_URL}/user/spotify_auth"


        setContent {
            CommentsContent(
                viewModel = viewModel(),
                postId = intent.getStringExtra("postId") ?: ""
            ) { goBack() }
        }
    }
}


@Composable
fun CommentsContent(viewModel: CommentsViewModel, postId: String, goBack: () -> Unit) {
    var newCommentText by remember { mutableStateOf("") }

    TuneInTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
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
                        .fillMaxWidth(0.85f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){Text("Comments", textAlign= TextAlign.Center)}
                }
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
//                    items(sampleComments) { comment ->
//                        CommentItem(comment)
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Divider()
//                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Write a comment...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        newCommentText = "" /* TODO */
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                ) {
                    Text("Post")
                }
            }
        }
    }
//
//    TuneInTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
////                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column() {
//                        IconButton(onClick = { goBack() }) {
//                            ca.uwaterloo.tunein.components.Icon(
//                                Icons.AutoMirrored.Filled.ArrowBack,
//                                contentDescription = "Go Back"
//                            )
//                        }
//                    }
//                    Column(modifier = Modifier
//                        .fillMaxWidth(0.85f),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ){Text("Settings", textAlign= TextAlign.Center)}
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                ){
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .fillMaxHeight(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//
//                    ){
////                        androidx.compose.material.Button(
////                            onClick = { showDialogUsername.value = true },
////                            colors = ButtonDefaults.buttonColors(
////                                backgroundColor = Color.DarkGreen
////                            ),
////                            modifier = Modifier
////                                .fillMaxWidth(0.8f)
////                                .height(IntrinsicSize.Max)
////                                .clip(RoundedCornerShape(10.dp))
////                        ) {
////                            Text(
////                                "Change Username",
////                                color = Color.TextBlack,
////                                fontSize = 20.sp,
////                                fontWeight = FontWeight.SemiBold
////                            )
////                        }
////                        Spacer(modifier = Modifier.height(32.dp))
////                        androidx.compose.material.Button(
////                            onClick = { showDialogPassword.value = true },
////                            colors = ButtonDefaults.buttonColors(
////                                backgroundColor = Color.DarkGreen
////                            ),
////                            modifier = Modifier
////                                .fillMaxWidth(0.8f)
////                                .height(IntrinsicSize.Max)
////                                .clip(RoundedCornerShape(10.dp))
////                        ) {
////                            Text(
////                                "Change Password",
////                                color = Color.TextBlack,
////                                fontSize = 20.sp,
////                                fontWeight = FontWeight.SemiBold
////                            )
////                        }
////                        Spacer(modifier = Modifier.height(32.dp))
////                        androidx.compose.material.Button(
////                            onClick = { showDialogPhoto.value = true },
////                            colors = ButtonDefaults.buttonColors(
////                                backgroundColor = Color.DarkGreen
////                            ),
////                            modifier = Modifier
////                                .fillMaxWidth(0.8f)
////                                .height(IntrinsicSize.Max)
////                                .clip(RoundedCornerShape(10.dp))
////                        ) {
////                            Text(
////                                "Update Photo",
////                                color = Color.TextBlack,
////                                fontSize = 20.sp,
////                                fontWeight = FontWeight.SemiBold
////                            )
////                        }
////                        Spacer(modifier = Modifier.height(32.dp))
////                        androidx.compose.material.Button(
////                            onClick = { },
////                            colors = ButtonDefaults.buttonColors(
////                                backgroundColor = Color.DarkGreen
////                            ),
////                            modifier = Modifier
////                                .fillMaxWidth(0.8f)
////                                .height(IntrinsicSize.Max)
////                                .clip(RoundedCornerShape(10.dp))
////                        ) {
////                            Text(
////                                "Reauthorize Spotify",
////                                color = Color.TextBlack,
////                                fontSize = 20.sp,
////                                fontWeight = FontWeight.SemiBold
////                            )
////                        }
//                    }
//                }
//            }
//
//        }
//    }
//
}
//
//@Composable
//fun CommentItem(comment: Comment) {
//    Column(modifier = Modifier.padding(vertical = 8.dp)) {
//        Text(
//            text = comment.username,
//            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
//        )
//        <<<<<<< HEAD
//        Spacer(modifier = Modifier.height(8.dp))
//        =======
//        >>>>>>> 42cdccc (rebase, fix merge conflicts)
//        Text(
//            text = comment.text,
//            style = MaterialTheme.typography.bodyMedium
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun CommentsContentPreview() {
//    // This is a sample list of comments for preview purposes
//    val sampleComments = listOf(
//        Comment(id = "1", postId = "post1", username = "User1", text = "This is a sample comment.", timestamp = 1588262400000),
//        Comment(id = "2", postId = "post1", username = "User2", text = "lol jdawg is so funny", timestamp = 1588348800000),
//        Comment(id = "3", postId = "post1", username = "User3", text = "great taste in music jdawg, *ok emoji*", timestamp = 1588262400000),
//    )
//
//    // Variable to hold the new comment text

//}
