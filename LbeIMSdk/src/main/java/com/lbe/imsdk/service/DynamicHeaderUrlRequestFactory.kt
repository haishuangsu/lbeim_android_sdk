package com.lbe.imsdk.service

import com.tinder.scarlet.websocket.okhttp.request.RequestFactory
import okhttp3.Request


internal class DynamicHeaderUrlRequestFactory(
    private val url: String,
    private val lbeToken: String,
    private val lbeSession: String,
) : RequestFactory {

    override fun createRequest(): Request = Request.Builder()
        .url(url)
        .addHeader("lbeToken", lbeToken)
        .addHeader("lbeSession", lbeSession)
        .build()
}
