package com.lbe.imsdk.model.req


data class InitMultiPartUploadBody(
    val size: Long,
    val name: String,
    val contentType: String,
)
