package n.learn.mdeditorapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import n.learn.mdeditorapp.ui.screens.*

@Composable
fun AppNavigation(startLoggedIn: Boolean) {
    val navController = rememberNavController()
    val start = if (startLoggedIn) "home" else "login"

    NavHost(navController = navController, startDestination = start) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                onGoRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("login") { popUpTo("register") { inclusive = true } } },
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(
                onOpenDocument = { docId -> navController.navigate("editor/$docId") },
                onOpenRemoteDocs = { navController.navigate("remote") },
                onLogout = { navController.navigate("login") { popUpTo("home") { inclusive = true } } }
            )
        }
        composable(
            route = "editor/{docId}",
            arguments = listOf(navArgument("docId") { type = NavType.IntType })
        ) { backStack ->
            val docId = backStack.arguments?.getInt("docId") ?: return@composable
            EditorScreen(
                docId = docId,
                onOpenChartBuilder = { navController.navigate("chart/$docId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "chart/{docId}",
            arguments = listOf(navArgument("docId") { type = NavType.IntType })
        ) { backStack ->
            val docId = backStack.arguments?.getInt("docId") ?: return@composable
            ChartBuilderScreen(
                onInsertChart = { imagePath ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("chart_image_path", imagePath)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("remote") {
            RemoteDocsScreen(
                onOpenDocument = { docId -> navController.navigate("editor/$docId") },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
