package info.hermiths.chatapp.ui.presentation.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import info.hermiths.chatapp.data.local.IMLocalRepository
import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.resp.MediaSource
import info.hermiths.chatapp.ui.presentation.components.DecryptedOrNotImageView
import info.hermiths.chatapp.ui.presentation.components.ExoPlayerView
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
    if (msgEntity.msgType == 2) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) { }
            DecryptedOrNotImageView(
                navController = navController,
                msgEntity,
                loadSource = true,
                fullScreen = true,
                fromMediaViewer = true,
                viewModel = null,
                imageLoader = imageLoader
            )
        }
    } else {
        var url = ""
        try {
            val media = Gson().fromJson(msgEntity.msgBody, MediaSource::class.java)
            url = media.resource.url
        } catch (e: Exception) {
            println("Json parse error -->> ${msgEntity.msgBody}")
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) { }
            ExoPlayerView(url)
        }
    }
}