package com.lbe.imsdk.model

import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class MessageEntity : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.invoke()

    var sessionId: String = ""
    var msgBody: String = ""
    var senderUid: String = ""
    var msgType: Int = 0
    var msgSeq: Int = 0

    var clientMsgID: String = ""

    var sendTime: Long = 0L

    // true: send success; false: send fail
    var sendSuccess: Boolean = true

    // true: read; false: no read yet
    var readed: Boolean = false

    var pendingUpload: Boolean = false

    var localFile: LocalMediaFile? = null

    var uploadTask: UploadTask? = null

    var timestamp: RealmInstant = RealmInstant.now()

    companion object {
        fun copy(source: MessageEntity): MessageEntity {
            val message = MessageEntity()
            message._id = source._id
            message.sessionId = source.sessionId
            message.msgType = source.msgType
            message.msgBody = source.msgBody
            message.senderUid = source.senderUid
            message.msgSeq = source.msgSeq
            message.clientMsgID = source.clientMsgID
            message.sendTime = source.sendTime
            message.sendSuccess = source.sendSuccess
            message.readed = source.readed
            message.pendingUpload = source.pendingUpload
            message.localFile = source.localFile
            message.uploadTask = source.uploadTask
            return message
        }
    }

    override fun toString(): String {
        return "MessageEntity(sessionId: $sessionId, senderUid: $senderUid, msgBody: $msgBody, msgType: $msgType, msgSeq: $msgSeq, clientMsgID: $clientMsgID, sendTime: $sendTime, sendSuccess: $sendSuccess, readed: $readed, pendingUpload: $pendingUpload, \n uploadTask: $uploadTask)"
    }
}

class LocalMediaFile : EmbeddedRealmObject {
    var fileName: String = ""
    var path: String = ""
    var size: Long = 0
    var isBigFile: Boolean = false
    var mimeType: String = ""
    var width: Int = 0
    var height: Int = 0
}

class UploadTask : EmbeddedRealmObject {
    var progress: Float = 0.0f
    var taskLength: Int = 0
    var executeIndex: Int = 0
    var initTrunksRepJson: String = ""
    var reqBodyJson: String = ""
    var lastTrunkUploadLength: Long = 0

    override fun toString(): String {
        return "UploadTask(progress:$progress, taskLength: $taskLength, executeIndex: $executeIndex,  lastTrunkUploadLength: $lastTrunkUploadLength,\n initTrunksRepJson --->>> $initTrunksRepJson,\n reqBodyJson --->>> $reqBodyJson)"
    }
}