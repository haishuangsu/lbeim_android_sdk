package com.lbe.imsdk.ui.presentation.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.toBitmap
import com.google.gson.Gson
import com.lbe.imsdk.R
import com.lbe.imsdk.model.MessageEntity
import com.lbe.imsdk.model.resp.MediaSource
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.CONTINUE_UPLOAD
import com.lbe.imsdk.utils.FileUtils


@Composable
fun ThumbDecryptedOrNotImageView(
    navController: NavController,
    message: MessageEntity,
    viewModel: ChatScreenViewModel?,
    imageLoader: ImageLoader
) {
    var thumbUrl = ""
    var thumbKey = ""
    var fullUrl = ""
    var fullKey = ""
    var width = 1080
    var height = 1920
    try {
        val media = Gson().fromJson(message.msgBody, MediaSource::class.java)
        fullUrl = media.resource.url
        fullKey = media.resource.key
        thumbUrl = media.thumbnail.url
        thumbKey = media.thumbnail.key
        width = media.width
        height = media.height
    } catch (e: Exception) {
        println("DecryptedOrNotImageView Json parse error -->> ${message.msgBody}")
    }

    println("Thumb 缩略图 --->> width: $width, height: $height, thumbUrl: $thumbUrl")
    val rememberProgress = remember { ChatScreenViewModel.progressList[message.clientMsgID] }
    val progress = rememberProgress?.collectAsState()
    val isGif = FileUtils.isGif(message.localFile?.mimeType ?: "") || FileUtils.isGif(fullUrl)

    Box(contentAlignment = Alignment.Center) {
        val ctx = LocalPlatformContext.current
        val culHeight = if (width > height) {
            110.dp
        } else {
            200.dp
        }

        val modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(culHeight)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                if (fullUrl.isNotEmpty()) {
                    navController.navigate("${NavRoute.MEDIA_VIEWER}/${message.clientMsgID}")
                } else {
                    Log.d(
                        CONTINUE_UPLOAD,
                        "断点暂停 --->>> canPending: ${message.canPending}, progress： ${message.uploadTask?.progress}"
                    )
                    if (message.localFile?.isBigFile == true && message.canPending) {
                        if (!message.pendingUpload && (message.uploadTask?.progress != 1.0f)) {
                            viewModel?.pendingUpload(message.clientMsgID, progress = progress)
                        } else {
                            Log.d(CONTINUE_UPLOAD, "续传 ---->>>> ${message.uploadTask}")
                            Log.d(
                                CONTINUE_UPLOAD, "续传 uri ---->>>> ${message.localFile?.path}"
                            )
                            Log.d(
                                CONTINUE_UPLOAD,
                                "续传 executeIndex: ${message.uploadTask?.executeIndex}"
                            )
                            val uri = Uri.parse(message.localFile?.path)
                            val inputStream = ctx.contentResolver.openInputStream(uri)
                            inputStream?.let { stream ->
                                try {
                                    viewModel?.continueSplitTrunksUpload(message, stream)
                                } catch (e: Exception) {
                                    println("$e")
                                }
                            }
                        }
                    }
                }
            }

        if (fullUrl.isEmpty()) {
            val context = LocalContext.current
            Log.d("ThumbnailGen", "Uri --->> ${message.localFile?.path ?: ""}")
            SubcomposeAsyncImage(
                modifier = modifier,
                model = ImageRequest.Builder(context).data(message.localFile?.path ?: "").build(),
                contentDescription = "Frame",
                contentScale = ContentScale.Crop,
                onSuccess = { state ->
                    progress?.value?.let {
                        if (it <= 0) {
                            val bitmap = state.result.image.toBitmap()
                            Log.d(
                                "ThumbnailGen",
                                "bitmap --->> width: ${bitmap.width}, height: ${bitmap.height}"
                            )
                            viewModel?.upload(message, bitmap, context)
                        }
                    }
                },
                loading = { },
                error = { },
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(if (isGif) fullUrl else thumbUrl).decoderFactory(
                        DecryptedDecoder.Factory(
                            url = if (isGif) fullUrl else thumbUrl,
                            key = if (isGif) fullKey else thumbKey
                        )
                    ).build(),
                contentDescription = "Yo",
                contentScale = ContentScale.Crop,
                modifier = modifier,
                imageLoader = imageLoader,
            )
        }

        if (fullUrl.isNotEmpty()) {
            if (message.msgType == 3) {
                Image(
                    painterResource(R.drawable.play),
                    contentDescription = "",
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            if (progress != null) {
                Log.d(
                    ChatScreenViewModel.UPLOAD,
                    "DecryptedOrNotImageView progress --->>> ${progress.value}, clientMsgID -->>> ${message.clientMsgID}, msg progress --->> ${message.uploadTask?.progress}"
                )
                if (progress.value != 1.0f) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = {
                                progress.value
                            },
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                            strokeWidth = 2.dp,
                        )

                        if (message.pendingUpload) {
                            Image(
                                painterResource(R.drawable.pending),
                                "",
                                modifier = Modifier.size(width = 8.dp, height = 13.dp)
                            )
                        } else {
                            val percent = "${progress.value * 100}"
                            Text(
                                "${
                                    percent.substring(
                                        0, if (percent.length > 5) 5 else percent.length
                                    )
                                }%", style = TextStyle(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.W600,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
                if (progress.value == 1.0f) {
                    if (message.msgType == 3) {
                        Image(
                            painterResource(R.drawable.play),
                            contentDescription = "",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}