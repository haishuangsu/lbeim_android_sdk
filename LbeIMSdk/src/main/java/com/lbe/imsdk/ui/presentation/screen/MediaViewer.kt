package com.lbe.imsdk.ui.presentation.screen

import ExoPlayerController
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.ImageLoader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lbe.imsdk.R
import com.lbe.imsdk.model.MessageEntity
import com.lbe.imsdk.model.resp.FaqAnswer
import com.lbe.imsdk.model.resp.FaqEntryUrl
import com.lbe.imsdk.model.resp.MediaSource
import com.lbe.imsdk.model.resp.Resource
import com.lbe.imsdk.model.resp.Thumbnail
import com.lbe.imsdk.ui.presentation.components.ExoPlayerView
import com.lbe.imsdk.ui.presentation.components.NormalDecryptedOrNotImageView
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.net.URL

@Composable
fun MediaViewer(
    navController: NavController,
    viewModel: ChatScreenViewModel,
    msgClientId: String,
    imageLoader: ImageLoader
) {
    val coroutineScope = rememberCoroutineScope()
    val saveSuccess = stringResource(R.string.save_success)

    println("NavTo, args: $msgClientId, viewModel msg size: ${viewModel.uiState.value?.messages?.size}")
    val cache = viewModel.uiState.value?.messages?.toMutableList()
    val messages: MutableList<MessageEntity> = mutableListOf()
    if (cache != null) {
        messages.addAll(cache)
    }
    val msgFilterSet: MutableList<MessageEntity> = mutableListOf()

    for (msg in messages) {
        when (msg.msgType) {
            2 -> {
                msgFilterSet.add(msg)
            }

            3 -> {
                msgFilterSet.add(msg)
            }

            10 -> {
                val faqAnswerType = object : TypeToken<MutableList<FaqAnswer>>() {}.type
                val faqAnswer = Gson().fromJson<MutableList<FaqAnswer>>(
                    msg.msgBody, faqAnswerType
                )
                var index = 0
                for (answerUnit in faqAnswer) {
                    if (answerUnit.type == 1) {
                        val faqEntryUrl =
                            Gson().fromJson(answerUnit.content, FaqEntryUrl::class.java)
                        Log.d("Faq", "Answer faqEntryUrl --->>> $faqEntryUrl")

                        val genMsg = MessageEntity()
                        genMsg.msgType = 2
                        val md = MediaSource(
                            width = 1,
                            height = 1,
                            Thumbnail(key = "", url = ""),
                            Resource(key = faqEntryUrl.key, url = faqEntryUrl.url)
                        )
                        genMsg.clientMsgID = "${msg.clientMsgID}_$index"
                        genMsg.msgBody = Gson().toJson(md)
                        msgFilterSet.add(genMsg)
                    }
                    index++
                }
            }
        }

        Log.d(
            "NavTo",
            "filter list ---->>> size: ${msgFilterSet.size}, ${msgFilterSet.map { m -> "msgClientId: ${m.clientMsgID}, ${m.msgBody}\n" }}"
        )
    }

    val targetEntity = msgFilterSet.find { it.clientMsgID == msgClientId }

    targetEntity?.let {
        val currentIndex = msgFilterSet.indexOf(it)
        val pagerState = rememberPagerState(initialPage = currentIndex, pageCount = {
            msgFilterSet.size
        })
        val ctx = LocalContext.current
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            HorizontalPager(state = pagerState) { page ->
                println("HorizontalPager ---> $page")
                MediaView(msgFilterSet[page], imageLoader)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 39.dp)
                    .align(Alignment.BottomCenter), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color(0xff979797).copy(alpha = 0.4f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(90.dp))
                        .clickable {
                            val msg = msgFilterSet[pagerState.currentPage]
                            var fullUrl = ""
                            var fullKey = ""
                            try {
                                val media = Gson().fromJson(msg.msgBody, MediaSource::class.java)
                                fullUrl = media.resource.url
                                fullKey = media.resource.key
                            } catch (e: Exception) {
                                println("DecryptedOrNotImageView Json parse error -->> ${msg.msgBody}")
                            }
                            if (msg.msgType == 2) {
                                val saveUrl = "$fullUrl?sign=$fullKey"
                                val extraFileName = extractFileName(saveUrl)
                                println("正则提取 --->>> $extraFileName")
                                coroutineScope.launch {
                                    val success = saveImageToGallery(
                                        ctx.contentResolver, saveUrl, extraFileName ?: "test.jpg"
                                    )
                                    if (success) {
                                        println("保存本地 图片成功 --->> $saveUrl")
                                        Toast
                                            .makeText(ctx, saveSuccess, Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        println("保存本地 图片失败 --->> $saveUrl")
                                    }
                                }
                            } else if (msg.msgType == 3) {
                                val saveUrl = "$fullUrl?sign=$fullKey"
                                val extraFileName = extractFileName(saveUrl)
                                coroutineScope.launch {
                                    val success = saveVideoToGallery(
                                        ctx.contentResolver, fullUrl, extraFileName ?: "test.mp4"
                                    )
                                    if (success) {
                                        println("保存本地 视频成功 --->> $saveUrl")
                                        Toast
                                            .makeText(ctx, saveSuccess, Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        println("保存本地 视频失败 --->> $saveUrl")
                                    }
                                }
                            }
                        },
                ) {
                    Text(
                        stringResource(R.string.save), style = TextStyle(
                            fontSize = 12.sp, fontWeight = FontWeight.W400, color = Color.White
                        ), modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.5.dp)
                    )
                }
                Box(modifier = Modifier.size(32.dp))

                Surface(color = Color(0xff979797).copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.popBackStack()
                        }) {
                    Image(
                        painter = painterResource(R.drawable.media_close),
                        contentDescription = stringResource(R.string.content_description_close),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(12.dp)
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaView(msgEntity: MessageEntity, imageLoader: ImageLoader) {
    var fullUrl = ""
    var fullKey = ""
    try {
        val media = Gson().fromJson(msgEntity.msgBody, MediaSource::class.java)
        fullUrl = media.resource.url
        fullKey = media.resource.key
    } catch (e: Exception) {
        println("DecryptedOrNotImageView Json parse error -->> ${msgEntity.msgBody}")
    }

    if (msgEntity.msgType == 2) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {}

            NormalDecryptedOrNotImageView(
                key = fullKey,
                url = fullUrl,
                modifier = Modifier.fillMaxWidth(),
                imageLoader = imageLoader
            )
        }
    } else {
        val context = LocalContext.current
        val playerController = remember {
            ExoPlayerController(context, fullUrl)
        }

        Box(
            Modifier
                .fillMaxSize()
                .clickable {
                    playerController.togglePlayPause()
                }, contentAlignment = Alignment.Center
        ) {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) { }

            ExoPlayerView(fullUrl, token = ChatScreenViewModel.lbeToken)
        }
    }
}

fun extractFileName(url: String): String? {
    val regex = """[^/]+(?:\.(?:jpg|jpeg|png|gif|mp4|avi|mov|webm|mkv|flv|bmp|tiff))""".toRegex()
    val matchResult = regex.find(url)
    return matchResult?.value
}


suspend fun saveImageToGallery(
    contentResolver: ContentResolver, imageUrl: String, fileName: String
): Boolean {
    val formatMap = mapOf(
        "jpg" to Pair("image/jpeg", Bitmap.CompressFormat.JPEG),
        "jpeg" to Pair("image/jpeg", Bitmap.CompressFormat.JPEG),
        "png" to Pair("image/png", Bitmap.CompressFormat.PNG),
        "webp" to Pair("image/webp", Bitmap.CompressFormat.WEBP),
        "gif" to Pair("image/gif", null) // GIF 不支持 Bitmap.CompressFormat
    )

    val fileExtension = fileName.substringAfterLast('.', "").lowercase()
    val formatInfo = formatMap[fileExtension] ?: return false

    val (mimeType, compressFormat) = formatInfo

    val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val uri = contentResolver.insert(imageCollection, contentValues) ?: return false

    return try {
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            if (compressFormat != null) {
                // 压缩并保存支持 Bitmap 的格式
                val bitmap = downloadBitmapFromUrl(imageUrl) ?: return false
                bitmap.compress(compressFormat, 100, outputStream)
            } else {
                // 直接保存 GIF 数据流
                saveRawImageStream(imageUrl, outputStream)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        }
        true
    } catch (e: IOException) {
        Log.e("SaveImage", "Error saving image", e)
        false
    }
}

suspend fun saveRawImageStream(imageUrl: String, outputStream: OutputStream): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            url.openStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
            true
        } catch (e: IOException) {
            Log.e("SaveRawImageStream", "Error saving raw image stream", e)
            false
        }
    }
}

suspend fun downloadBitmapFromUrl(imageUrl: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            BitmapFactory.decodeStream(url.openStream())
        } catch (e: IOException) {
            Log.e("DownloadBitmap", "Error downloading image", e)
            null
        }
    }
}

suspend fun saveVideoToGallery(
    contentResolver: ContentResolver, videoUrl: String, fileName: String
): Boolean {
    val formatMap = mapOf(
        "mp4" to "video/mp4",
        "mkv" to "video/x-matroska",
        "webm" to "video/webm",
        "avi" to "video/x-msvideo",
        "mov" to "video/quicktime",
        "flv" to "video/x-flv",
        "wmv" to "video/x-ms-wmv"
    )

    val fileExtension = fileName.substringAfterLast('.', "").lowercase()
    val mimeType = formatMap[fileExtension] ?: "video/*"
    println("保存本地 视频 mimeType --->>> $mimeType")

    return withContext(Dispatchers.IO) {
        try {
            val urlConnection = URL(videoUrl).openConnection()
            urlConnection.connectTimeout = 10_000 // 10秒超时
            urlConnection.readTimeout = 30_000 // 30秒超时
            val inputStream = urlConnection.getInputStream()

            val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val uri =
                contentResolver.insert(videoCollection, contentValues) ?: return@withContext false

            return@withContext try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    inputStream.use { it.copyTo(outputStream) }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }
                true
            } catch (e: IOException) {
                Log.e("SaveVideo", "Error saving video to gallery", e)
                false
            } finally {
                inputStream.close()
            }
        } catch (e: IOException) {
            Log.e("SaveVideo", "Error downloading video", e)
            false
        }
    }
}




