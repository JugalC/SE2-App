package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.data.User
import ca.uwaterloo.tunein.messaging.Firebase
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.ui.theme.TuneInTheme
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

private val showDialogUsername = mutableStateOf(false)
private val showDialogPassword = mutableStateOf(false)
private val showDialogPhoto = mutableStateOf(false)
private val showDialogAuth = mutableStateOf(false)
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun goBack() {
            finish()
        }

        // create volley queue
        val queue = Volley.newRequestQueue(this)
        // url for updating username
        val usernameURL = "${BuildConfig.BASE_URL}/user/update_user"
        // url for updating password
        val passwordURL = "${BuildConfig.BASE_URL}/user/update_password"
        // url for updating photo
        val photoURL = "${BuildConfig.BASE_URL}/user/update_photo"
        // url for reauthorizing spotify
        // val spotifyURL = "${BuildConfig.BASE_URL}/user/spotify_auth"

        fun onConfirmationUsername(u: String) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")
            val user = AuthManager.getUser(this)
            if (user.username == u.toString()) {
                alert.setMessage("Username is the same as the current username")
                alert.create().show()
                return
            }
            val req = JSONObject()
            req.put("currUsername", user.username)
            req.put("newUsername", u)

            val userUpdateReq = JsonObjectRequest(
                Request.Method.POST, usernameURL, req,
                { updateRes ->
                    val userLocal = AuthManager.getUser(this)
                    AuthManager.setUser(this, User(
                        id=userLocal.id,
                        username=updateRes.getString("newUsername"),
                        firstName=userLocal.firstName,
                        lastName=userLocal.lastName
                    ))
                    showDialogUsername.value = false
                },
                { error ->
                    val statusCode: Int = error.networkResponse.statusCode
                    if (statusCode == 400) {
                        alert.setMessage("username already exists, try another")
                        alert.create().show()
                    } else {
                        Log.e("Settings", error.toString())
                        alert.setMessage("An unexpected error has occurred")
                        alert.create().show()
                    }
                }
            )
            queue.add(userUpdateReq)

        }

        fun onDismissRequestUsername() {
            showDialogUsername.value = false
        }

        fun onConfirmationPassword(pass: String, confirmPass: String) {
            val alert = android.app.AlertDialog.Builder(this).setTitle("Error")
            if (pass != confirmPass) {
                alert.setMessage("Passwords do not match")
                alert.create().show()
                return
            }

            val user = AuthManager.getUser(this)

            val req = JSONObject()
            req.put("username", user.username)
            req.put("newPassword", pass)

            val userUpdateReq = JsonObjectRequest(
                Request.Method.POST, passwordURL, req,
                { _ ->
                    val positiveAlert = android.app.AlertDialog.Builder(this).setTitle("Success")
                    positiveAlert.setMessage("Password has been updated")
                    positiveAlert.create().show()
                    showDialogPassword.value = false
                },
                { error ->
                    Log.e("Settings", error.toString())
                    alert.setMessage("An unexpected error has occurred")
                    alert.create().show()
                }
            )
            queue.add(userUpdateReq)
        }

        fun onDismissRequestPassword() {
            showDialogPassword.value = false
        }


        


        setContent {
            SettingsContent(
                goBack ={goBack()},
                onConfirmationUsername = ::onConfirmationUsername,
                onDismissRequestUsername={onDismissRequestUsername()},
                onConfirmationPassword =::onConfirmationPassword
            ) { onDismissRequestPassword() }
        }
    }
}

@Composable
fun SettingsContent(
    goBack: () -> Unit,
    onConfirmationUsername: (String) -> Unit,
    onDismissRequestUsername: () -> Unit,
    onConfirmationPassword: (String, String) -> Unit,
    onDismissRequestPassword: () -> Unit
) {
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
//                    horizontalArrangement = Arrangement.SpaceBetween,
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
                    ){Text("Settings", textAlign= TextAlign.Center)}
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center

                    ){
                        Button(
                            onClick = {  showDialogUsername.value = true},
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGreen),
                            modifier = Modifier.fillMaxWidth(0.8f).height(IntrinsicSize.Max).clip(RoundedCornerShape(10.dp))
                        ) {
                            Text("Change Username", color = Color.TextBlack, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { showDialogPassword.value = true},
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGreen),
                            modifier = Modifier.fillMaxWidth(0.8f).height(IntrinsicSize.Max).clip(RoundedCornerShape(10.dp))
                        ) {
                            Text("Change Password", color = Color.TextBlack, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGreen),
                            modifier = Modifier.fillMaxWidth(0.8f).height(IntrinsicSize.Max).clip(RoundedCornerShape(10.dp))
                        ) {
                            Text("Update Photo", color = Color.TextBlack, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGreen),
                            modifier = Modifier.fillMaxWidth(0.8f).height(IntrinsicSize.Max).clip(RoundedCornerShape(10.dp))
                        ) {
                            Text("Reauthorize Spotify", color = Color.TextBlack, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

        }
    }
    if(showDialogUsername.value) {
        DialogChangeUsername(onDismissRequestUsername, onConfirmationUsername)
    }
    if(showDialogPassword.value) {
        DialogChangePassword(onDismissRequestPassword, onConfirmationPassword)
    }
}

@Composable
fun DialogChangeUsername(
    onDismissRequestUsername: () -> Unit,
    onConfirmationUsername: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismissRequestUsername() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.DarkGray,
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize( ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                     text = "Current Username: " + AuthManager.getUser(LocalContext.current).username,
                     style = TextStyle(
                         color = Color.LightGray
                     )
                )
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            text = "New Username",
                            style = TextStyle(
                                color = Color.LightGray
                            )
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.LightGreen,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.LightGreen,
                        unfocusedLabelColor = Color.LightGray,
                        disabledBorderColor = Color.LightGray
                        ),
                    textStyle = TextStyle.Default.copy(color = Color.LightGray),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequestUsername() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss", color=Color.LightGreen)
                    }
                    TextButton(
                        onClick = { onConfirmationUsername(username) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm", color=Color.LightGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogChangePassword(
    onDismissRequestPassword: () -> Unit,
    onConfirmationPassword: (String, String) -> Unit
) {
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { onDismissRequestPassword() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth().height(IntrinsicSize.Max)
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
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it},
                    visualTransformation = PasswordVisualTransformation(),
                    label = {
                        Text(
                            text = "New Password",
                            style = TextStyle(
                                color = Color.LightGray
                            )
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.LightGreen,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.LightGreen,
                        unfocusedLabelColor = Color.LightGray,
                        disabledBorderColor = Color.LightGray
                    ),
                    textStyle = TextStyle.Default.copy(color = Color.LightGray),
                )
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it},
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "Confirm New Password", style = TextStyle(
                        color = Color.LightGray
                    )) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.LightGreen,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.LightGreen,
                        unfocusedLabelColor = Color.LightGray,
                        disabledBorderColor = Color.LightGray
                    ),
                    textStyle = TextStyle.Default.copy(color = Color.LightGray),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequestPassword() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss", color=Color.LightGreen)
                    }
                    TextButton(
                        onClick = { onConfirmationPassword(pass,confirmPass) },
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
fun SettingsView() {
    SettingsContent ({}, {}, {}, { _: String, _: String -> }){}
}


