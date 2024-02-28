package ca.uwaterloo.tunein.auth

import android.content.Context

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
        fun getUsername(context: Context): String {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString("username", "")!!
        }
        fun setUsername(context: Context, username: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("username", username).apply()
        }

    }
}
