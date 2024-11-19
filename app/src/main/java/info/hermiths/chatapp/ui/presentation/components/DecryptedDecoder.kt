package info.hermiths.chatapp.ui.presentation.components

import android.util.Log
import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel
import okio.Buffer
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DecryptedDecoder(
    private val result: SourceFetchResult,
    private val options: Options,
    private val imageLoader: ImageLoader,
) : Decoder {
    override suspend fun decode(): DecodeResult? {
        val defaultImageLoader = ImageLoader.Builder(options.context).build()
        return defaultImageLoader.components.newDecoder(
            result, options, imageLoader
        )?.first?.decode()
    }

    class Factory(private val url: String, private val key: String) : Decoder.Factory {
        override fun create(
            result: SourceFetchResult, options: Options, imageLoader: ImageLoader
        ): Decoder {
            var newResult = result
            if (key.isNotEmpty()) {
                Log.d(
                    ChatScreenViewModel.IMAGEENCRYPTION,
                    "Decrypted image ---->>> url: $url ,key: $key, options: $options"
                )
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                val secretKey = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
                val iv = IvParameterSpec(ByteArray(16))
                cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

                val source = result.source
                val respData = source.source().readByteArray()
                Log.d(ChatScreenViewModel.IMAGEENCRYPTION, "repData --->>> ${respData.size}")
                val decryptedData = cipher.doFinal(Base64.getDecoder().decode(respData))
                Log.d(
                    ChatScreenViewModel.IMAGEENCRYPTION,
                    "decryptedData size: ${decryptedData.size},  decryptedData: $decryptedData"
                )
                val buffer = Buffer().apply {
                    write(decryptedData)
                }
                val newSource = ImageSource(buffer, source.fileSystem, source.metadata)
                newResult = SourceFetchResult(newSource, result.mimeType, result.dataSource)
            }
            return DecryptedDecoder(newResult, options, imageLoader)
        }
    }
}

