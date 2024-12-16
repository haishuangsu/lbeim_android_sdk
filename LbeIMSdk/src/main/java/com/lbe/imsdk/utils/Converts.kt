package com.lbe.imsdk.utils

import android.util.Log
import com.lbe.imsdk.model.MessageEntity
import com.lbe.imsdk.model.proto.IMMsg
import com.lbe.imsdk.model.req.MsgBody
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.lbeSession
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.seq
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.uid
import com.lbe.imsdk.utils.TimeUtils.timeStampGen
import com.lbe.imsdk.utils.UUIDUtils.uuidGen

object Converts {

    fun sendBodyToEntity(body: MsgBody): MessageEntity {
        val entity = MessageEntity().apply {
            sessionId = lbeSession
            senderUid = uid
            msgBody = body.msgBody
            msgType = body.msgType
            clientMsgID = body.clientMsgId
            sendTime = body.clientMsgId.split("-").last().toLong()
            msgSeq = seq
        }
        Log.d("Convert sendToEntity", entity.toString())
        return entity
    }

    fun protoToEntity(proto: IMMsg.MsgBody): MessageEntity {
        val entity = MessageEntity().apply {
            sessionId = proto.sessionId.ifEmpty { lbeSession }
            senderUid = proto.senderUid
            msgBody = proto.msgBody
            msgType = when (proto.msgType) {
                IMMsg.MsgType.TextMsgType -> 1
                IMMsg.MsgType.ImgMsgType -> 2
                IMMsg.MsgType.VideoMsgType -> 3
                IMMsg.MsgType.FaqMsgType -> 8
                IMMsg.MsgType.KnowledgePointMsgType -> 9
                IMMsg.MsgType.KnowledgeAnswerMsgType -> 10
                else -> 15
            }
            msgSeq = proto.msgSeq
            clientMsgID = proto.clientMsgID.ifEmpty { "${uuidGen()}_${timeStampGen()}" }
            sendTime = proto.clientMsgID.split("-").last().toLong()
        }
        Log.d("Convert protoToEntity", entity.toString())
        return entity
    }

    fun entityToSendBody(entity: MessageEntity, newClientMsgID: String): MsgBody {
        return MsgBody(
            msgBody = entity.msgBody,
            msgSeq = entity.msgSeq,
            msgType = entity.msgType,
            clientMsgId = newClientMsgID,
            sendTime = newClientMsgID.split("-").last(),
            source = 100
        )
    }

    fun entityToMediaSendBody(entity: MessageEntity): MsgBody {
        return MsgBody(
            msgBody = entity.msgBody,
            msgSeq = entity.msgSeq,
            msgType = entity.msgType,
            clientMsgId = entity.clientMsgID,
            sendTime = entity.clientMsgID.split("-").last(),
            source = 100
        )
    }
}