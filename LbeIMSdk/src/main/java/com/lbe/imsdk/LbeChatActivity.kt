package com.lbe.imsdk

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.video.VideoFrameDecoder
import com.google.gson.Gson
import com.lbe.imsdk.model.InitArgs
import com.lbe.imsdk.ui.presentation.components.NavRoute

import com.lbe.imsdk.ui.presentation.screen.ChatScreen
import com.lbe.imsdk.ui.presentation.screen.MediaViewer
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel
import info.hermiths.lbesdk.ui.theme.ChatAppTheme

class LbeChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initArgsJson = intent.getStringExtra("initArgs")
        val initArgs = Gson().fromJson(initArgsJson, InitArgs::class.java)
        Log.d("LbeIMSdk", "Sdk 接收 args --->> $initArgs")
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
                    add(VideoFrameDecoder.Factory())
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
                    composable(route = NavRoute.CUSTOM_REFRESH) {
                    }
                }
            }
        }
    }
}