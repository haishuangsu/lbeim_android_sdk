package com.lbe.imsdk.utils

import android.util.Log
import com.lbe.imsdk.model.MessageEntity
import com.lbe.imsdk.model.proto.IMMsg
import com.lbe.imsdk.model.req.MsgBody
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.lbeSession
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.seq
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.uid

object Converts {

    fun sendBodyToEntity(body: MsgBody): MessageEntity {
        val entity = MessageEntity().apply {
            sessionId = lbeSession
            senderUid = uid
            msgBody = body.msgBody
            msgType = body.msgType
            clientMsgID = body.clientMsgId
            sendTime = body.sendTime.toLong()
            msgSeq = seq
        }
        Log.d("Convert sendToEntity", entity.toString())
        return entity
    }

    fun protoTypeConvert(proto: IMMsg.MsgEntityToFrontEnd): Int {
        val convertMsgType = when (proto.msgType) {
            IMMsg.MsgType.TextMsgType -> when (proto.msgBody.msgType) {
                IMMsg.ContentType.TextContentType -> 1
                IMMsg.ContentType.ImgContentType -> 2
                IMMsg.ContentType.VideoContentType -> 3
                IMMsg.ContentType.AgentUserJoinSessionContentType -> 5 // 客服接入(只推送给C端)
                IMMsg.ContentType.EndSessionContentType -> 6 // 会话结束(只推送给C端)
                IMMsg.ContentType.RankingContentType -> 7  // 排队
                IMMsg.ContentType.FaqContentType -> 8
                IMMsg.ContentType.KnowledgePointContentType -> 9
                IMMsg.ContentType.KnowledgeAnswerContentType -> 10
                IMMsg.ContentType.TransferContentType -> 11 // 转接
                IMMsg.ContentType.SystemContentType -> 12
                IMMsg.ContentType.UnsupportedContentType -> 13 // 无客服在线
                else -> 39
            }

            else -> 39
        }
        return convertMsgType
    }

    fun protoToEntity(proto: IMMsg.MsgEntityToFrontEnd): MessageEntity {
        val loadMsgType = protoTypeConvert(proto)

        val entity = MessageEntity().apply {
            sessionId = proto.msgBody.sessionId.ifEmpty { lbeSession }
            senderUid = proto.msgBody.senderUid
            msgBody = proto.msgBody.msgBody
            msgType = loadMsgType
            msgSeq = proto.msgBody.msgSeq
            clientMsgID = proto.msgBody.clientMsgID
            sendTime = proto.msgBody.sendTime.toLong()
            faqListTile = proto.msgBody.title
            customerServiceNickname = proto.msgBody.senderNickname
            customerServiceAvatar = proto.msgBody.senderFaceURL
        }
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
            sendTime = entity.sendTime.toString(),
            source = 100
        )
    }
}