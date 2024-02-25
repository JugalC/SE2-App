package ca.uwaterloo.tunein.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ca.uwaterloo.tunein.MyApplication
import ca.uwaterloo.tunein.auth.AuthManager

class AuthViewModel : ViewModel() {
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    init {
        // Set initial state
        _isLoggedIn.value = AuthManager.isLoggedIn(MyApplication.instance)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        _isLoggedIn.value = isLoggedIn
        AuthManager.setLoggedIn(MyApplication.instance, isLoggedIn)
    }
}