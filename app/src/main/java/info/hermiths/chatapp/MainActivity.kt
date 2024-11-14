package info.hermiths.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import info.hermiths.chatapp.ui.presentation.components.NavRoute

import info.hermiths.chatapp.ui.presentation.screen.ChatScreen
import info.hermiths.chatapp.ui.presentation.screen.MediaViewer
import info.hermiths.chatapp.ui.theme.ChatAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController, startDestination = NavRoute.CHAT
                ) {
                    composable(route = NavRoute.CHAT) {
                        ChatScreen(navController)
                    }
                    composable(route = "${NavRoute.MEDIA_VIEWER}/{msgClientId}") { navBackStackEntry ->
                        val msgClientId = navBackStackEntry.arguments?.getString("msgClientId")
                        msgClientId?.let {
                            MediaViewer(
                                navController, msgClientId
                            )
                        }
                    }
                }
            }
        }
    }
}