package info.hermiths.chatapp.model

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

//    var mediaSrc: MediaSrc? = null

    var timestamp: RealmInstant = RealmInstant.now()

    override fun toString(): String {
        return "MessageEntity(sessionId: $sessionId, senderUid: $senderUid, msgBody: $msgBody, msgType: $msgType, msgSeq: $msgSeq, clientMsgID: $clientMsgID, sendStamp: $sendStamp, sendSuccess: $sendSuccess, readed: $readed)"
    }
}

//class MediaSrc : EmbeddedRealmObject {
//    var width: Int = 0
//    var height: Int = 0
//    var thumbnail: Thumb? = null
//    var resource: Rsc? = null
//}
//
//class Thumb : EmbeddedRealmObject {
//    var url: String = ""
//    var key: String = ""
//}
//
//class Rsc : EmbeddedRealmObject {
//    var url: String = ""
//    var key: String = ""
//}