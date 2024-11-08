package info.hermiths.chatapp.model

import info.hermiths.chatapp.model.resp.MediaSource
import java.io.File

data class MediaMessage(
    val file: File, val isImage: Boolean = false, var mediaSource: MediaSource? = null
)

