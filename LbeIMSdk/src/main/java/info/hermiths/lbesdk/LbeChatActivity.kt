package info.hermiths.lbesdk

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import com.google.gson.Gson
import info.hermiths.lbesdk.model.InitArgs
import info.hermiths.lbesdk.ui.presentation.components.NavRoute

import info.hermiths.lbesdk.ui.presentation.screen.ChatScreen
import info.hermiths.lbesdk.ui.presentation.screen.MediaViewer
import info.hermiths.lbesdk.ui.presentation.viewmodel.ChatScreenViewModel
import info.hermiths.lbesdk.ui.theme.ChatAppTheme

class LbeChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initArgsJson = intent.getStringExtra("initArgs")
        val initArgs = Gson().fromJson(initArgsJson, InitArgs::class.java)
        Log.d("LbeIMSdk", "Sdk 接收 args --->> $initArgs")
        enableEdgeToEdge()
        setContent {
            ChatAppTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val viewModel: ChatScreenViewModel = viewModel()
                viewModel.initSdk(initArgs)
                val gifImageLoader = ImageLoader.Builder(context).components {
                    if (SDK_INT >= 28) {
                        add(AnimatedImageDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }.build()

                NavHost(
                    navController = navController, startDestination = NavRoute.CHAT
                ) {
                    composable(route = NavRoute.CHAT) {
                        ChatScreen(navController, viewModel, imageLoader = gifImageLoader)
                    }
                    composable(route = "${NavRoute.MEDIA_VIEWER}/{msgClientId}") { navBackStackEntry ->
                        val msgClientId = navBackStackEntry.arguments?.getString("msgClientId")
                        msgClientId?.let {
                            MediaViewer(
                                navController, viewModel, msgClientId, gifImageLoader
                            )
                        }
                    }
                }
            }
        }
    }

    private fun enableEdgeToEdge() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}