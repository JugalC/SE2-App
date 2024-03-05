package ca.uwaterloo.tunein.auth

import android.content.Context
import ca.uwaterloo.tunein.data.User

private const val PREF_NAME = "auth_pref"
private val KEY_IS_LOGGED_IN = "is_logged_in"
private val KEY_IS_SPOTIFY_AUTH = "is_spotify_auth"

class AuthManager {
    companion object {
        fun isLoggedIn(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_IS_LOGGED_IN, false)

        }
        fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
        }
        fun isSpotifyAuthed(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_IS_SPOTIFY_AUTH, false)
        }
        fun setSpotifyAuthed(context: Context, isSpotifyAuthed: Boolean) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_IS_SPOTIFY_AUTH, isSpotifyAuthed).apply()
        }
        fun getUser(context: Context): User {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val id = prefs.getString("id", "")!!
            val username = prefs.getString("username", "")!!
            val firstName = prefs.getString("firstName", "")!!
            val lastName = prefs.getString("lastName", "")!!
            return User(id, username, firstName, lastName)
        }
        fun setUser(context: Context, user: User) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs
                .edit()
                .putString("id", user.id)
                .putString("username", user.username)
                .putString("firstName", user.firstName)
                .putString("lastName", user.lastName)
                .apply()
        }

    }
}
