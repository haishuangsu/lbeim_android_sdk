package info.hermiths.chatapp.ui.presentation.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.gson.Gson
import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.resp.MediaSource
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel.Companion.IMAGEENCRYPTION
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MsgTypeContent(
    message: MessageEntity, context: Context,
) {
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
            var url = ""
            var key = ""
            try {
                val media = Gson().fromJson(message.msgBody, MediaSource::class.java)
                url = media.resource.url
                key = media.resource.key
            } catch (e: Exception) {
                Log.d("", "")
            }
            Log.d(IMAGEENCRYPTION, "url: $url, key: $key")

            if (key.isEmpty()) {
                GlideImage(
                    model = url,
                    contentDescription = "Yo",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(width = 160.dp, height = 90.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            } else {
                val secretByteArray = key.toByteArray(StandardCharsets.UTF_8)
                val secretKey = SecretKeySpec(secretByteArray, "AES")
                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                val ivP = IvParameterSpec(ByteArray(16))
                // decryption
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivP)
//                val deData = cipher.doFinal(ByteArray(32)) // put data

//            // gen key
//                val keyGenerator = KeyGenerator.getInstance("AES")
//                keyGenerator.init(256)
//                val secretKey = keyGenerator.generateKey()

                // encryption
//                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivP)
//                val enData = cipher.doFinal(ByteArray(32)) // put data


//                GlideImage(
//                    model = deData,
//                    contentDescription = "Yo",
//                    contentScale = ContentScale.FillBounds,
//                    modifier = Modifier
//                        .size(width = 160.dp, height = 90.dp)
//                        .clip(RoundedCornerShape(16.dp)),
//                ) {
//                    it.decode()
//                }

//                val imageLoader = ImageLoader.Builder(context).components {
////                        add(CustomCacheInterceptor())
////                        add(ItemMapper())
////                        add(HttpUrlKeyer())
////                        add(CronetFetcher.Factory())
//                    add(BitmapFactoryDecoder.Factory())
//                }.build()
//
//                Image(
//                    painter = rememberAsyncImagePainter(
//                        model = ImageRequest.Builder(context).data(url)
////                            .decoderFactory(DecryptionDecoder.Factory(key = key))
////                            .build(), imageLoader = imageLoader
//                    ), contentDescription = ""
//                )

                GlideImage(
                    model = url,
                    contentDescription = "Yo",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(width = 160.dp, height = 90.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }

        }

        3 -> {
            var url = ""
            try {
                val media = Gson().fromJson(message.msgBody, MediaSource::class.java)
                url = media.resource.url
            } catch (e: Exception) {

            }
            ExoPlayerView(url)
        }

        else -> {
            Text("Not implement yet. --->>> { ${message.msgType} }")
        }
    }
}
