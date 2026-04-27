package org.remess.mdeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.remess.mdeditor.navigation.AppNavigation
import org.remess.mdeditor.ui.theme.MDEditorappTheme
import org.remess.mdeditor.util.SessionManager

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
