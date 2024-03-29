package ca.uwaterloo.tunein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.uwaterloo.tunein.data.Comment
import ca.uwaterloo.tunein.viewmodel.CommentsViewModel
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CommentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: CommentsViewModel = viewModel()
            val postId = intent.getStringExtra("postId") ?: ""
            CommentsContent(viewModel, postId)
        }
    }
}

@Composable
fun CommentsContent(viewModel: CommentsViewModel, postId: String) {
    val comments by viewModel.getCommentsForPost(postId).collectAsState(initial = emptyList())
    var newCommentText by remember { mutableStateOf("") }

    TuneInTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(26.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(comments) { comment ->
                        CommentItem(comment)
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Input field for new comment
                TextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add a comment...") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Button to submit the comment
                Button(
                    onClick = { 
                        viewModel.addComment(postId, newCommentText)
                        newCommentText = "" // Clear the input field after submission
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    ) {
                    Text("Post")
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = comment.username,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = comment.text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommentsContentPreview() {
    // This is a sample list of comments for preview purposes
    val sampleComments = listOf(
        Comment(id = "1", postId = "post1", username = "User1", text = "This is a sample comment.", timestamp = 1588262400000),
        Comment(id = "2", postId = "post1", username = "User2", text = "lol jdawg is so funny", timestamp = 1588348800000),
        Comment(id = "3", postId = "post1", username = "User3", text = "great taste in music jdawg, *ok emoji*", timestamp = 1588262400000),
        )

    // Variable to hold the new comment text
    var newCommentText by remember { mutableStateOf("") }

    TuneInTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(26.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(sampleComments) { comment ->
                        CommentItem(comment)
                        Spacer(modifier = Modifier.height(20.dp))
//                        Divider()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Input field for new comment
                TextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add a comment...") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Button to submit the comment
                Button(
                    onClick = { 
                        // In the preview, we just clear the input field
                        newCommentText = ""
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    ) {
                    Text("Post")
                }
            }
        }
    }
}
