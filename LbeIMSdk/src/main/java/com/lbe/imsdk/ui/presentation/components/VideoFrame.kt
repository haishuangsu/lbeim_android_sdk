package com.lbe.imsdk.ui.presentation.components

import android.util.Log

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.toBitmap

@Composable
fun VideoFrameBitmapCompose(uri: String) {
    val context = LocalContext.current
    Log.d("VideoFrame", "Uri --->> $uri")
    SubcomposeAsyncImage(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .heightIn(max = 180.dp)
            .clip(RoundedCornerShape(16.dp)),

        model = ImageRequest.Builder(context).data(uri).build(),
        contentDescription = "Video Frame",
        contentScale = ContentScale.Crop,
        onSuccess = { state ->
            val bitmap = state.result.image.toBitmap()
            Log.d("VideoFrame", "bitmap --->> width: ${bitmap.width}, height: ${bitmap.height}")
        },
        loading = { },
        error = { },
    )

}