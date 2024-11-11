package info.hermiths.chatapp.ui.presentation.components

import android.util.Log
import androidx.compose.runtime.key
import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel

class DecryptionDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val key: String,
) : Decoder {
    override suspend fun decode(): DecodeResult? {
        Log.d(ChatScreenViewModel.IMAGEENCRYPTION, "key: $key")

//        return DecodeResult()
        return null
    }

    class Factory(val key: String) : Decoder.Factory {
        override fun create(
            result: SourceFetchResult, options: Options, imageLoader: ImageLoader
        ): Decoder {
            return DecryptionDecoder(source = result.source, options = options, key = key)
        }
    }
}