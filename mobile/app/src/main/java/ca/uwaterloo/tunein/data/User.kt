package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = ""
)