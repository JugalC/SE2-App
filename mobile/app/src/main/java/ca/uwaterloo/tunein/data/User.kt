package ca.uwaterloo.tunein.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = ""
)