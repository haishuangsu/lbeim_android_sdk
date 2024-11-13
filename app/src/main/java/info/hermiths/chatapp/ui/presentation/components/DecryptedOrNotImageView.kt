package info.hermiths.chatapp.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.google.gson.Gson
import info.hermiths.chatapp.model.resp.MediaSource


@Composable
fun DecryptedOrNotImageView(msgBody: String) {
    var url = ""
    var key = ""
    try {
        val media = Gson().fromJson(msgBody, MediaSource::class.java)
        url = media.resource.url
        key = media.resource.key
    } catch (e: Exception) {
        println("Json parse error -->> $msgBody")
    }
    if (key.isEmpty()) {
        AsyncImage(
            model = url,
            contentDescription = "Yo",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(width = 160.dp, height = 90.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    } else {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalPlatformContext.current).data(url)
                    .decoderFactory(DecryptedDecoder.Factory(url = url, key = key))
                    .build(),
            ),
            contentDescription = "Yo",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(width = 160.dp, height = 90.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}