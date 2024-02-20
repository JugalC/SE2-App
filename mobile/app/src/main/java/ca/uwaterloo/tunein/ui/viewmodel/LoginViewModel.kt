package ca.uwaterloo.tunein.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginState(
    val email: String = "",
    val password: String = "",
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun setEmail(newEmail: String) {
        _uiState.update { currentState ->
            currentState.copy(email = newEmail)
        }
    }

    fun setPassword(newPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(password = newPassword)
        }
    }
}