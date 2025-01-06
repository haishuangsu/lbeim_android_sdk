package com.lbe.imsdk.ui.presentation.components

import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.forEach
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R as ExoPlayerUiR

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(url: String) {
    val context = LocalContext.current

    val exoPlayer = ExoPlayer.Builder(context).build()

    val mediaSource = remember(url) {
        MediaItem.fromUri(url)
    }

    LaunchedEffect(mediaSource) {
        exoPlayer.setMediaItem(mediaSource)
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
//                    logChildViewIds(this)
                    findViewById<View>(ExoPlayerUiR.id.exo_prev)?.visibility = View.GONE
                    findViewById<View>(ExoPlayerUiR.id.exo_next)?.visibility = View.GONE
                    findViewById<View>(ExoPlayerUiR.id.exo_rew_with_amount)?.visibility = View.GONE
                    findViewById<View>(ExoPlayerUiR.id.exo_ffwd_with_amount)?.visibility = View.GONE
//                    findViewById<View>(ExoPlayerUiR.id.exo_settings)?.visibility = View.GONE
                }
            }, modifier = Modifier.matchParentSize()
        )
    }
}

private fun logChildViewIds(viewGroup: ViewGroup) {
    viewGroup.forEach { child ->
        if (child is ViewGroup) {
            logChildViewIds(child)
        } else {
            try {
                println(
                    "ExoPlayerView --->>> View: ${child.javaClass.simpleName}, ID: ${
                        if (child.id != View.NO_ID) child.resources.getResourceName(child.id) else "NO_ID"
                    }"
                )
            } catch (e: Exception) {
                println("ExoPlayerView --->>> View: ${child.javaClass.simpleName}, ID: UNKNOWN")
            }
        }
    }
}