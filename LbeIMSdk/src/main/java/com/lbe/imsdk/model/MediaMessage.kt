package com.lbe.imsdk.model

import com.lbe.imsdk.model.resp.MediaSource

data class MediaMessage(
//    val file: File,
    val fileName: String,
    val path: String = "",
    val fileSize: Long = 0,
    val width: Int,
    val height: Int,
    val isImage: Boolean = false,
    val mime: String = "",
    var mediaSource: MediaSource? = null
)

