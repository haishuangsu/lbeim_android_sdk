package com.lbe.imsdk.ui.presentation.components

import ExoPlayerController
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun NewExoPlayerView(playerController: ExoPlayerController) {
    DisposableEffect(Unit) {
        onDispose {
            playerController.release()
        }
    }

    AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
        PlayerView(ctx).apply {
            player = playerController.player
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
    })
}