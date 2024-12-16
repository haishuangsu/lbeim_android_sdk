package com.lbe.imsdk.model

import com.lbe.imsdk.model.resp.MediaSource
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

