package ca.uwaterloo.tunein.auth

import android.content.Context

private const val PREF_NAME = "auth_pref"
private const val KEY_IS_LOGGED_IN = "is_logged_in"

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
    }
}
