package com.lbe.imsdk.model

data class InitArgs(
    val lbeSign: String,
    val lbeIdentity: String,
    val nickId: String,
    val nickName: String,
    val phone: String,
    val email: String,
    val headerIcon: String,
    val language: String,
    val device: String,
    val source: String,
//    val extraInfo: MutableMap<String, *>?,
)