package com.lbe.imsdk

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.lbe.imsdk.model.InitArgs

object LbeSdk {
    fun init(
        context: Context,
        nickId: String,
        nickName: String,
        lbeIdentity: String,
        lbeSign: String,
        phone: String,
        email: String,
        language: String,
        device: String
    ) {
        val initArgs = InitArgs(
            lbeSign = lbeSign,
            lbeIdentity = lbeIdentity,
            nickId = nickId,
            nickName = nickName,
            phone = phone,
            email = email,
            headerIcon = "",
            language = language,
            device = device,
            source = "",
//                        extraInfo = mutableMapOf(),
        )
        val intent = Intent(context, LbeChatActivity::class.java).putExtra(
            "initArgs", Gson().toJson(initArgs)
        )
        context.startActivity(intent)
    }
}