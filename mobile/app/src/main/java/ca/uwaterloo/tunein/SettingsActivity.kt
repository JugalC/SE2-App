package ca.uwaterloo.tunein

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextStyle
import ca.uwaterloo.tunein.auth.AuthManager
import ca.uwaterloo.tunein.components.Icon
import ca.uwaterloo.tunein.ui.theme.Color
import ca.uwaterloo.tunein.ui.theme.TuneInTheme

private val showDialogUsername = mutableStateOf(false)
private val showDialogPassword = mutableStateOf(false)

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = AuthManager.getUser(this)

        fun goBack() {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        fun onConfirmationUsername() {
//            val intent = Intent(this, PostsActivity::class.java)
            showDialogUsername.value = false
//            startActivity(intent)
        }

        fun onDismissRequestUsername() {
            showDialogUsername.value = false
        }

        fun onConfirmationPassword() {
//            val intent = Intent(this, PostsActivity::class.java)
            showDialogPassword.value = false
//            startActivity(intent)
        }

        fun onDismissRequestPassword() {
            showDialogPassword.value = false
        }



        setContent {
            SettingsContent(
                goBack={goBack()},
                onConfirmationUsername={onConfirmationUsername()},
                onDismissRequestUsername={onDismissRequestUsername()},
                onConfirmationPassword={onConfirmationPassword()},
                onDismissRequestPassword={onDismissRequestPassword()}
            )
        }
    }
}

@Composable
fun SettingsContent(
    goBack: () -> Unit,
    onConfirmationUsername: () -> Unit,
    onDismissRequestUsername: () -> Unit,
    onConfirmationPassword: () -> Unit,
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
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Text("Change Username", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { showDialogPassword.value = true},
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGreen),
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Text("Change Password", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGreen),
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Text("Change Photo", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.DarkGreen),
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Text("Reauthorize Spotify", fontSize = 16.sp)
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
    onConfirmationUsername: () -> Unit
) {

    Dialog(onDismissRequest = { onDismissRequestUsername() }) {
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
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
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
                        onClick = { onConfirmationUsername() },
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
    onConfirmationPassword: () -> Unit
) {

    Dialog(onDismissRequest = { onDismissRequestPassword() }) {
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
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
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
                        onClick = { onConfirmationPassword() },
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
    SettingsContent ({}, {}, {}, {}){}
}


