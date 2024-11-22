package info.hermiths.chatapp.model

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

//    var sendStamp: Long = 0L

    var sendTime: Long = 0L

    // true: send success; false: send fail
    var sendSuccess: Boolean = true

    // true: read; false: no read yet
    var readed: Boolean = false

    var pendingUpload: Boolean = false

    var localFile: LocalMediaFile? = null

    var uploadTask: UploadTask? = null

    var timestamp: RealmInstant = RealmInstant.now()

    override fun toString(): String {
        return "MessageEntity(sessionId: $sessionId, senderUid: $senderUid, msgBody: $msgBody, msgType: $msgType, msgSeq: $msgSeq, clientMsgID: $clientMsgID, sendTime: $sendTime, sendSuccess: $sendSuccess, readed: $readed)"
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