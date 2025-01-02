package com.lbe.imsdk.ui.presentation.components

import android.util.Log
import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel
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
//        val newOptions = options.copy(size = Size(300, 500))
//        val result = if (!DecodeUtils.isGif(result.source.source())) {
//            if (SDK_INT >= 28) {
//                AnimatedImageDecoder(result.source, newOptions).decode()
//            } else {
//                GifDecoder(result.source, newOptions).decode()
//            }
//        } else {
//            val defaultImageLoader = ImageLoader.Builder(newOptions.context).build()
//            defaultImageLoader.components.newDecoder(
//                result, newOptions, imageLoader
//            )?.first?.decode()
//        }
//        return result
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
            Log.d(
                ChatScreenViewModel.IMAGE_ENCRYPTION,
                "Decrypted image ---->>> url: $url ,key: $key, \noptions: $options"
            )
            if (key.isNotEmpty()) {
                Log.d(
                    ChatScreenViewModel.IMAGE_ENCRYPTION,
                    "Decrypted image ---->>> url: $url ,key: $key, 执行解密"
                )
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                val secretKey = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
                val iv = IvParameterSpec(ByteArray(16))
                cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

                val source = result.source
                val respData = source.source().readByteArray()
                Log.d(ChatScreenViewModel.IMAGE_ENCRYPTION, "repData --->>> ${respData.size}")
                val decryptedData = cipher.doFinal(Base64.getDecoder().decode(respData))
                Log.d(
                    ChatScreenViewModel.IMAGE_ENCRYPTION,
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

