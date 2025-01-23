package com.lbe.imsdk.model.req

data class SessionBody(
    val extraInfo: String,
    val headIcon: String,
    val nickId: String,
    val nickName: String,
    val phone: String,
    val email: String,
    val uid: String,
    val language: String,
    val device: String,
    val source: String,
    val identityID: String,
    val groupID: String,
)

