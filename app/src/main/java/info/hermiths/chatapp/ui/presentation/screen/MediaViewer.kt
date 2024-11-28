package info.hermiths.chatapp.ui.presentation.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.ImageLoader
import com.google.gson.Gson
import info.hermiths.chatapp.data.local.IMLocalRepository
import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.resp.MediaSource
import info.hermiths.chatapp.ui.presentation.components.ExoPlayerView
import info.hermiths.chatapp.ui.presentation.components.NormalDecryptedOrNotImageView
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel


@Composable
fun MediaViewer(navController: NavController, msgClientId: String, imageLoader: ImageLoader) {
    println("NavTo, args: $msgClientId")
    val msgEntity = IMLocalRepository.findMsgByClientMsgId(msgClientId)
    msgEntity?.let {
        val msgSet = IMLocalRepository.findAllMediaMessages(msgEntity.sessionId ?: "")
        Log.d(
            ChatScreenViewModel.REALM,
            "图片视频集 --->>> ${msgSet.map { msg -> "(${msg.msgSeq}, ${msg.msgBody}\n" }}"
        )
        val currentIndex = msgSet.indexOf(msgEntity)
        val pagerState = rememberPagerState(initialPage = currentIndex, pageCount = {
            msgSet.size
        })
        HorizontalPager(state = pagerState) { page ->
            println("HorizontalPager ---> $page")
            MediaView(navController, msgSet[page], imageLoader)
        }
    }
}

@Composable
fun MediaView(navController: NavController, msgEntity: MessageEntity, imageLoader: ImageLoader) {
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
                key = fullKey, url = fullUrl,
                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(500.dp)
                    .clickable {},
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