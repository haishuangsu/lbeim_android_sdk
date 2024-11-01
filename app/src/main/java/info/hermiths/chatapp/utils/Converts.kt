package info.hermiths.chatapp.utils

import android.util.Log
import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.proto.IMMsg
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel.Companion.lbeSession
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel.Companion.seq
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel.Companion.uid
import info.hermiths.chatapp.utils.TimeUtils.timeStampGen
import info.hermiths.chatapp.utils.UUIDUtils.uuidGen

object Converts {

    fun sendBodyToEntity(body: MsgBody): MessageEntity {
        val entity = MessageEntity().apply {
            sessionId = lbeSession
            senderUid = uid
            msgBody = body.msgBody
            msgType = body.msgType
            clientMsgID = body.clientMsgId
            sendStamp = body.clientMsgId.split("-").last().toLong()
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
                else -> 9
            }
            msgSeq = proto.msgSeq
            clientMsgID = proto.clientMsgID.ifEmpty { "${uuidGen()}_${timeStampGen()}" }
            sendStamp = clientMsgID.split("-").last().toLong()
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
            source = 100
        )
    }
}