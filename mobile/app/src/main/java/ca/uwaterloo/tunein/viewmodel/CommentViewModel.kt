package ca.uwaterloo.tunein.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.tunein.data.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentsViewModel : ViewModel() {
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    init {
        // Sample data
        val sampleComments = listOf(
            Comment(id = "1", postId = "post1", username = "User1", text = "This is a sample comment.", timestamp = 1588262400000),
            Comment(id = "2", postId = "post1", username = "User2", text = "lol jdawg is so funny", timestamp = 1588348800000),
            Comment(id = "3", postId = "post1", username = "User3", text = "great taste in music jdawg, *ok emoji*", timestamp = 1588262400000),
        )
        viewModelScope.launch {
            _comments.emit(sampleComments)
        }
    }
    fun addComment(postId: String, text: String) {
        val newComment = Comment(
            id = (comments.value.size + 1).toString(),
            postId = postId,
            username = "CurrentUsername", 
            text = text,
            timestamp = System.currentTimeMillis()
        )

        // Add the new comment to the existing list
        val updatedComments = comments.value.toMutableList().apply {
            add(newComment)
        }
        viewModelScope.launch {
            _comments.emit(updatedComments)
        }
    }

    fun getCommentsForPost(postId: String): StateFlow<List<Comment>> {
        //fetch comments from backend
        return comments
    }
}
