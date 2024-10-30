package info.hermiths.chatapp.utils

import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.proto.IMMsg
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel.Companion.lbeSession
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel.Companion.seq
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel.Companion.uid

object Converts {

    fun sendBodyToEntity(body: MsgBody): MessageEntity {
        return MessageEntity().apply {
            sessionId = lbeSession
            senderUid = uid
            msgBody = body.msgBody
            msgType = body.msgType
            clientMsgID = body.clientMsgId
            msgSeq = seq
        }
    }

    fun protoToEntity(proto: IMMsg.MsgBody): MessageEntity {
        return MessageEntity().apply {
            senderUid = proto.senderUid
            msgBody = proto.msgBody
            msgType = when (proto.msgType) {
                IMMsg.MsgType.TextMsgType -> 1
                IMMsg.MsgType.ImgMsgType -> 2
                IMMsg.MsgType.VideoMsgType -> 3
                else -> 9
            }
            msgSeq = proto.msgSeq
            clientMsgID = proto.clientMsgID
        }
    }

    fun entityToSendBody(entity: MessageEntity): MsgBody {
        return MsgBody(
            msgBody = entity.msgBody,
            msgSeq = entity.msgSeq,
            msgType = entity.msgType,
            clientMsgId = entity.clientMsgID,
            source = 100
        )
    }
}