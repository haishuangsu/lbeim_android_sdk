package info.hermiths.chatapp.ui.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import info.hermiths.chatapp.model.proto.IMMsg
import info.hermiths.chatapp.ui.data.model.ChatMessage


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MsgTypeContent(message: ChatMessage) {
    when (message.msgType) {
        IMMsg.MsgType.TextMsgType -> {
            Surface(
                color = Color.White, modifier = Modifier.clip(
                    RoundedCornerShape(
                        topEnd = 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp,
                    )
                )
            ) {
                Text(
                    text = message.message, modifier = Modifier.padding(12.dp), style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff000000)
                    )
                )
            }
        }

        IMMsg.MsgType.ImgMsgType -> {
            GlideImage(
                model = message.message,
                contentDescription = "Yo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            )
        }

        IMMsg.MsgType.VideoMsgType -> {
            ExoPlayerView(message.message)
        }

        else -> {
            Text("Not implement yet. --->>> { ${message.msgType} }")
        }
    }
}
