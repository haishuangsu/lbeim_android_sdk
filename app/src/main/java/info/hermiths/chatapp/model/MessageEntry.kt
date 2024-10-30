package info.hermiths.chatapp.model

import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.Index
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

    @Index
    var clientMsgID: String = ""

    @Ignore // true: send success; false: send fail
    var sendSuccess: Boolean = true

    @Ignore // true: read; false: no read yet
    var readed: Boolean = false

    var timestamp: RealmInstant = RealmInstant.now()

    override fun toString(): String {
        return "MessageEntity(sessionId: $sessionId, senderUid: $senderUid, msgBody: $msgBody, msgType: $msgType, msgSeq: $msgSeq, clientMsgID: $clientMsgID, sendSuccess: $sendSuccess, readed: $readed)"
    }
}