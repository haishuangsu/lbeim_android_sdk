package info.hermiths.chatapp.model

import info.hermiths.chatapp.model.resp.Resource
import info.hermiths.chatapp.model.resp.Thumbnail
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

    var sendStamp: Long = 0L

    var sendTime: String = ""

    // true: send success; false: send fail
    var sendSuccess: Boolean = true

    // true: read; false: no read yet
    var readed: Boolean = false

    var mediaSrc: MediaSrc? = null

    var timestamp: RealmInstant = RealmInstant.now()

    override fun toString(): String {
        return "MessageEntity(sessionId: $sessionId, senderUid: $senderUid, msgBody: $msgBody, msgType: $msgType, msgSeq: $msgSeq, clientMsgID: $clientMsgID, sendStamp: $sendStamp, sendSuccess: $sendSuccess, readed: $readed)"
    }
}

class MediaSrc : EmbeddedRealmObject {
    val width: Int = 0
    val height: Int = 0
    val thumbnail: Thumb? = null
    val resource: Rsc? = null
}

class Thumb : EmbeddedRealmObject {
    val url: String = ""
    val key: String = ""
}

class Rsc : EmbeddedRealmObject {
    val url: String = ""
    val key: String = ""
}