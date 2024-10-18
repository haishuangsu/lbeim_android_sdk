package info.hermiths.chatapp.ui.data.model

import com.google.gson.Gson


data class ChatMessage(
    val fromUser: String,
    val message: String
)

fun ChatMessage.toJsonString(): String = Gson().toJson(this).toString()