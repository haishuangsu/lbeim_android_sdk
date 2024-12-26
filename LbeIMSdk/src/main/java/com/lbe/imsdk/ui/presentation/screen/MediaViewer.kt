package com.lbe.imsdk.ui.presentation.screen

import ExoPlayerController
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp
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

@Composable
fun MediaViewer(
    navController: NavController,
    viewModel: ChatScreenViewModel,
    msgClientId: String,
    imageLoader: ImageLoader
) {
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
                    .padding(start = 16.dp, end = 16.dp, bottom = 39..dp)
                    .align(Alignment.BottomCenter), horizontalArrangement = Arrangement.SpaceBetween
            ) {
//                Surface(color = Color(0xff979797).copy(alpha = 0.4f),
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(90.dp))
//                        .clickable {
//
//                        }) {
//                    Text(
//                        "保存", style = TextStyle(
//                            fontSize = 12.sp, fontWeight = FontWeight.W400, color = Color.White
//                        ), modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.5.dp)
//                    )
//                }
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
                        contentDescription = "",
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

//            NewExoPlayerView(playerController)
//
//            if (!playerController.isPlaying) {
//                Image(
//                    painterResource(R.drawable.play),
//                    contentDescription = "",
//                    modifier = Modifier
//                        .size(53.dp)
//                        .align(Alignment.Center),
//                )
//            }
//
//            CustomProgressBar(
//                progress = if (playerController.durationInSeconds > 0)
//                    playerController.currentSecond.toFloat() / playerController.durationInSeconds
//                else 0f,
//                bufferedProgress = if (playerController.durationInSeconds > 0)
//                    playerController.bufferedPositionInSeconds.toFloat() / playerController.durationInSeconds
//                else 0f,
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(start = 20.dp, end = 20.dp, bottom = 59.dp)
//                    .fillMaxWidth()
//                    .height(32.dp),
//                onValueChange = { value ->
//                    val newPosition = (value * playerController.durationInSeconds).toInt()
//                    playerController.seekTo(newPosition)
//                }
//            )
//
////            BufferedProgressBar(progress = if (playerController.durationInSeconds > 0) playerController.currentSecond.toFloat() / playerController.durationInSeconds
////            else 0f,
////                bufferedProgress = if (playerController.durationInSeconds > 0) playerController.bufferedPositionInSeconds.toFloat() / playerController.durationInSeconds
////                else 0f,
////                modifier = Modifier
////                    .align(Alignment.BottomCenter)
////                    .padding(bottom = 59.dp)
////                    .fillMaxWidth()
////                    .height(19.dp),
////                onValueChange = { value ->
////                    val newPosition = (value * playerController.durationInSeconds).toInt()
////                    playerController.seekTo(newPosition)
////                })
//
//            Row(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 99.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
//                    Button(
//                        onClick = { playerController.changePlaybackSpeed(speed) },
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(horizontal = 4.dp)
//                    ) {
//                        Text("${speed}x")
//                    }
//                }
//            }

            ExoPlayerView(fullUrl)
        }
    }
}

@Composable
fun BufferedProgressBar(
    progress: Float, // 播放进度 (0f..1f)
    bufferedProgress: Float, // 缓冲进度 (0f..1f)
    modifier: Modifier, onValueChange: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .padding(vertical = 8.dp)
    ) {
        // 缓冲进度条
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // 缓冲进度条背景
            drawRect(
                color = androidx.compose.ui.graphics.Color.LightGray,
                size = androidx.compose.ui.geometry.Size(width, height)
            )

            // 缓冲进度
            drawRect(
                color = androidx.compose.ui.graphics.Color.Gray,
                size = androidx.compose.ui.geometry.Size(width * bufferedProgress, height)
            )

            // 播放进度
            drawRect(
                color = androidx.compose.ui.graphics.Color.White,
                size = androidx.compose.ui.geometry.Size(width * progress, height)
            )
        }

        // 拖动条
        Slider(
            value = progress,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.matchParentSize(),
            colors = SliderDefaults.colors(
                thumbColor = androidx.compose.ui.graphics.Color.White,
                activeTrackColor = androidx.compose.ui.graphics.Color.Transparent,
                inactiveTrackColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
    }
}


@Composable
fun CustomProgressBar(
    progress: Float, // 当前播放进度 (0f..1f)
    bufferedProgress: Float, // 缓冲进度 (0f..1f)
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit
) {
    // 获取当前 Density
    val density = LocalDensity.current
    val thumbRadius = with(density) { 12.dp.toPx() } // 圆点半径

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        Canvas(modifier = Modifier
            .matchParentSize()
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val x = change.position.x
                    val newValue = (x / size.width).coerceIn(0f, 1f)
                    onValueChange(newValue)
                }
            }) {
            val width = size.width
            val height = size.height
            val progressX = width * progress
            val bufferedX = width * bufferedProgress

            // 绘制缓冲背景
            drawRect(
                color = androidx.compose.ui.graphics.Color.LightGray,
                size = androidx.compose.ui.geometry.Size(width, height / 4),
                topLeft = androidx.compose.ui.geometry.Offset(0f, height / 2 - height / 8)
            )

            // 绘制缓冲区域
            drawRect(
                color = androidx.compose.ui.graphics.Color.Gray,
                size = androidx.compose.ui.geometry.Size(bufferedX, height / 4),
                topLeft = androidx.compose.ui.geometry.Offset(0f, height / 2 - height / 8)
            )

            // 绘制播放区域
            drawRect(
                color = androidx.compose.ui.graphics.Color.Blue,
                size = androidx.compose.ui.geometry.Size(progressX, height / 4),
                topLeft = androidx.compose.ui.geometry.Offset(0f, height / 2 - height / 8)
            )

            // 绘制圆点（thumb）
            drawCircle(
                color = androidx.compose.ui.graphics.Color.Blue,
                radius = thumbRadius,
                center = androidx.compose.ui.geometry.Offset(progressX, height / 2)
            )
        }
    }
}

