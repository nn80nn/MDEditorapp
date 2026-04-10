package n.learn.mdeditorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import n.learn.mdeditorapp.navigation.AppNavigation
import n.learn.mdeditorapp.ui.theme.MDEditorappTheme
import n.learn.mdeditorapp.util.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val session = SessionManager(this)
        setContent {
            MDEditorappTheme {
                AppNavigation(startLoggedIn = session.isLoggedIn())
            }
        }
    }
}
