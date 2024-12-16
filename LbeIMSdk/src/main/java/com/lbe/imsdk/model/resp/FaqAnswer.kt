package com.lbe.imsdk.model.resp

data class FaqAnswer(
    val type: Int,
    val content: String,
//    var contents: MutableList<LinkText>,
    var contents: String,
)

data class LinkText(
    val content: String,
    val url: String,
)