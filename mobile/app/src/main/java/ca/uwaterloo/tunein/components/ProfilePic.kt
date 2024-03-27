package ca.uwaterloo.tunein.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun ProfilePic(url: String, modifier: Modifier) {
    AsyncImage(
        model = url,
        contentDescription = "Profile Pic",
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}