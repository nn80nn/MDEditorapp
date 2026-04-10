package n.learn.mdeditorapp.util

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("mdeditor_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? = prefs.getString("jwt_token", null)

    fun getBearerToken(): String? = getToken()?.let { "Bearer $it" }

    fun clearToken() {
        prefs.edit().remove("jwt_token").apply()
    }

    fun isLoggedIn() = getToken() != null
}
