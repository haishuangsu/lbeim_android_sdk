package info.hermiths.lbesdk.model

import info.hermiths.lbesdk.model.resp.MediaSource
import java.io.File

data class MediaMessage(
    val file: File,
    val isImage: Boolean = false,
    val mime: String = "",
    val width: Int,
    val height: Int,
    val path: String = "",
    var mediaSource: MediaSource? = null
)

