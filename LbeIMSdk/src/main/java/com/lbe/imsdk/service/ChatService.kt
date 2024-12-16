package com.lbe.imsdk.service

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable

interface ChatService {

    @Receive
    fun observeConnection(): Flowable<WebSocket.Event>

    @Send
    fun sendMessage(buffer: ByteArray)

}