package com.lbe.imsdk.ui.presentation.screen

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
                    .padding(start = 16.dp, end = 16.dp, bottom = 19.dp)
                    .align(Alignment.BottomCenter), horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Surface(color = Color(0xff979797).copy(alpha = 0.4f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(90.dp))
                        .clickable {

                        }) {
                    Text(
                        "保存", style = TextStyle(
                            fontSize = 12.sp, fontWeight = FontWeight.W400, color = Color.White
                        ), modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.5.dp)
                    )
                }

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
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) { }
            ExoPlayerView(fullUrl)
        }
    }
}