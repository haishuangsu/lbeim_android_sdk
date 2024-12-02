package info.hermiths.lbesdk.ui.presentation.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth


import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import coil3.ImageLoader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import info.hermiths.lbesdk.model.MessageEntity
import info.hermiths.lbesdk.model.resp.FaqAnswer
import info.hermiths.lbesdk.model.resp.FaqEntryUrl
import info.hermiths.lbesdk.model.resp.MediaSource
import info.hermiths.lbesdk.model.resp.Resource
import info.hermiths.lbesdk.model.resp.Thumbnail
import info.hermiths.lbesdk.ui.presentation.components.ExoPlayerView
import info.hermiths.lbesdk.ui.presentation.components.NormalDecryptedOrNotImageView
import info.hermiths.lbesdk.ui.presentation.viewmodel.ChatScreenViewModel


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
        HorizontalPager(state = pagerState) { page ->
            println("HorizontalPager ---> $page")
            MediaView(msgFilterSet[page], imageLoader)
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
                key = fullKey, url = fullUrl, modifier = Modifier
                    .fillMaxWidth()
                    .clickable {}, imageLoader = imageLoader
            )
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) { }
            ExoPlayerView(fullUrl)
        }
    }
}