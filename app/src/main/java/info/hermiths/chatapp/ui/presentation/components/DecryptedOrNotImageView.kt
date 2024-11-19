package info.hermiths.chatapp.ui.presentation.components

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
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.google.gson.Gson
import info.hermiths.chatapp.R
import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.resp.MediaSource
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel

@Composable
fun DecryptedOrNotImageView(
    navController: NavController,
    message: MessageEntity,
    loadSource: Boolean = false,
    fullScreen: Boolean = true,
    fromMediaViewer: Boolean = false,
    viewModel: ChatScreenViewModel?,
) {
    var thumbUrl = ""
    var thumbKey = ""
    var fullUrl = ""
    var fullKey = ""
    var isBigFile = false
    try {
        val media = Gson().fromJson(message.msgBody, MediaSource::class.java)
        fullUrl = media.resource.url
        fullKey = media.resource.key
        thumbUrl = media.thumbnail.url
        thumbKey = media.thumbnail.key
        isBigFile = media.isBigFile
    } catch (e: Exception) {
        println("DecryptedOrNotImageView Json parse error -->> ${message.msgBody}")
    }

    val progress = ChatScreenViewModel.progressList[message.clientMsgID]?.collectAsState()
    Log.d(
        ChatScreenViewModel.UPLOAD,
        "DecryptedOrNotImageView progress --->>> ${progress?.value}, clientMsgID -->>> ${message.clientMsgID}, msg progress --->> ${message.progress}"
    )

    Box(contentAlignment = Alignment.Center) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(if (loadSource) fullUrl else thumbUrl).decoderFactory(
                        DecryptedDecoder.Factory(
                            url = if (loadSource) fullUrl else thumbUrl,
                            key = if (loadSource) fullKey else thumbKey
                        )
                    ).build(),
            ),
            contentDescription = "Yo",
            contentScale = ContentScale.FillBounds,
            modifier = if (fullScreen) Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clickable {
                    // TODO
                } else Modifier
                .size(
                    width = 160.dp, height = 90.dp
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    if (!fromMediaViewer) {
                        if (fullUrl.isNotEmpty()) {
                            navController.navigate("${NavRoute.MEDIA_VIEWER}/${message.clientMsgID}")
                        } else {
                            if (isBigFile) {
                                // TODO stop the trunks upload
                                Log.d(
                                    ChatScreenViewModel.UPLOAD,
                                    "Tab, 缓存的进度： ${message.progress}"
                                )
                                viewModel?.cancelJob(
                                    message.clientMsgID, progress = progress
                                )
                            }
                        }
                    }
                },
        )

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