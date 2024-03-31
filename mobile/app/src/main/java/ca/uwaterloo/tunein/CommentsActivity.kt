package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.data.Comment
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.viewmodel.CommentsViewModel
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import ca.uwaterloo.tunein.viewmodel.FeedViewModel
import ca.uwaterloo.tunein.viewmodel.FriendsViewModel
import coil.compose.AsyncImage
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject

class CommentsActivity : ComponentActivity() {

    private val commentsViewModel by viewModels<CommentsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val postId = intent.getStringExtra("postId") ?: ""
        commentsViewModel.pullComments(postId, this)

        val queue = Volley.newRequestQueue(this)
        val commentUrl = "${BuildConfig.BASE_URL}/comment/$postId"


        fun handleClickProfile(userId: String) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_profile", userId)
            startActivity(intent)
        }

        fun handleAddComment(content: String) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")
            if (content.isEmpty()) {
                alert.setMessage("Comment cannot be empty")
                alert.show()
                return
            }
            val ctx = this
            val req = JSONObject()
            Log.i("CommentsActivity", "content: $content")

            req.put("content", content)
            val addCommentReq = object:JsonObjectRequest(Request.Method.POST, commentUrl, req,
                { response ->
                    commentsViewModel.pullComments(postId, this)
                },
                { error ->
                    Log.e("CommentsActivity", "Error adding comment: $error")
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
            queue.add(addCommentReq)

//            commentsViewModel.pullComments(postId, this)
        }

        fun handleDeleteComment(commentId: String) {
            // TODO actual delete part
            commentsViewModel.pullComments(postId, this)
        }

        Log.i("CommentsActivity", "postId: $postId")


        fun goBack() {
            finish()
        }

        setContent {
            CommentsContent(
                viewModel = viewModel(),
                postId = postId,
                handleClickProfile = ::handleClickProfile,
                handleAddComment = ::handleAddComment,
                handleDeleteComment = ::handleDeleteComment,
            ) { goBack() }
        }
    }
}


@Composable
fun CommentsContent(viewModel: CommentsViewModel, postId: String, handleClickProfile: (userId: String) -> Unit, handleAddComment: (content : String) -> Unit, handleDeleteComment: (commentId : String) -> Unit, goBack: () -> Unit) {
    var newCommentText by remember { mutableStateOf("") }
    val comments by viewModel.comments.collectAsStateWithLifecycle()

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
                    items(comments.comments) { comment ->
                        CommentItem(comment, handleClickProfile)
                        Spacer(modifier = Modifier.height(6.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(6.dp))
                    }
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
                        handleAddComment(newCommentText);
                        newCommentText = ""
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
}
@Composable
fun CommentItem(comment: Comment,  handleClickProfile: (userId: String) -> Unit) {

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row (modifier = Modifier.clickable { handleClickProfile(comment.userId) })
        {
            AsyncImage(
                model = comment.profilePicture,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .align(Alignment.Top)
            )
            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    text = "@" + comment.username,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }

    }
}
