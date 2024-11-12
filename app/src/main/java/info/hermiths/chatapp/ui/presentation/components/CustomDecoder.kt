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

class CustomDecoder(
    private val result: SourceFetchResult,
    private val options: Options,
    private val imageLoader: ImageLoader
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
            Log.d(ChatScreenViewModel.IMAGEENCRYPTION, "url: $url ,key: $key, options: $options")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

            val secretKey = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            Log.d(ChatScreenViewModel.IMAGEENCRYPTION, "key utf_8 处理 --->>> $secretKey")

            val iv = IvParameterSpec(ByteArray(16)) // 初始化 IV（与 CryptoJS 使用 "0000000000000000" 相同）
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

            val source = result.source
            val repData = source.source().readByteArray()
            Log.d(ChatScreenViewModel.IMAGEENCRYPTION, "repData --->>> ${repData.size}")
            val decryptedData = cipher.doFinal(repData)
            Log.d(ChatScreenViewModel.IMAGEENCRYPTION, "decryptedData --->>> ${decryptedData.size}")
            Log.d(ChatScreenViewModel.IMAGEENCRYPTION, "执行了吗")

            val suffix: String = getFileSuffix(url)
            val base64Image =
                "data:image/$suffix;base64," + Base64.getEncoder().encodeToString(decryptedData)
            Log.d(ChatScreenViewModel.IMAGEENCRYPTION, "base64Image --->>> $base64Image")

            val buffer = Buffer().apply {
                write(decryptedData)
//                write(base64Image.toByteArray())
//                write(Base64.getEncoder().encode(base64Image.toByteArray()))
            }
            val newSource = ImageSource(buffer, source.fileSystem, source.metadata)
            val newResult = SourceFetchResult(newSource, result.mimeType, result.dataSource)
            return CustomDecoder(newResult, options, imageLoader)
        }

        private fun getFileSuffix(url: String): String {
            val imgSuffix = arrayOf(".jpg", ".jpeg", ".png", ".gif") // 支持的图片格式
            for (suffix in imgSuffix) {
                if (url.contains(suffix)) {
                    return suffix.substring(1) // 去掉 "."
                }
            }
            return "png" // 默认图片格式
        }
    }
}

