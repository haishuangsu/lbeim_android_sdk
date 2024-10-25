package info.hermiths.chatapp.ui.data.model

import com.google.gson.Gson
import info.hermiths.chatapp.model.proto.IMMsg


data class ChatMessage(
    val fromUser: String,
    val message: String,
    val msgType: IMMsg.MsgType,
)

fun ChatMessage.toJsonString(): String = Gson().toJson(this).toString()