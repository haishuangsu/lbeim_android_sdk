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
import info.hermiths.chatapp.model.MessageEntity

enum class ConnectionStatus {
    NOT_STARTED, OPENED, CLOSED, CONNECTING, CLOSING, FAILED, RECEIVED
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MsgTypeContent(message: MessageEntity) {
    when (message.msgType) {
        1 -> {
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
                    text = message.msgBody, modifier = Modifier.padding(12.dp), style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff000000)
                    )
                )
            }
        }

        2 -> {
            GlideImage(
                model = message.msgBody,
                contentDescription = "Yo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            )
        }

        3 -> {
            ExoPlayerView(message.msgBody)
        }

        else -> {
            Text("Not implement yet. --->>> { ${message.msgType} }")
        }
    }
}
