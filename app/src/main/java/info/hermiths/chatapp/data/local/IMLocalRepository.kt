package info.hermiths.chatapp.data.local

import android.util.Log
import info.hermiths.chatapp.model.MessageEntity
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import org.mongodb.kbson.ObjectId

object IMLocalRepository {
    private val realm = RealmInstance.realm

    fun filterMessages(sessionId: String): List<MessageEntity> {
        return realm.query<MessageEntity>(
            query = "sessionId == $0", sessionId
        ).sort("sendStamp", Sort.ASCENDING).find()
    }

    fun findMsgByClientMsgId(clientMsgID: String): MessageEntity? {
        return realm.query<MessageEntity>(
            query = "clientMsgID == $0", clientMsgID
        ).first().find()
    }

    fun findAllMediaMessages(sessionId: String): List<MessageEntity> {
        return realm.query<MessageEntity>(
            query = "sessionId == $0 AND (msgType == 2 || msgType == 3)", sessionId
        ).sort("sendStamp", Sort.ASCENDING).find()
    }

    suspend fun findMsgAndSetStatus(clientMsgID: String, success: Boolean) {
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.sendSuccess = success
        }
    }

    suspend fun findMsgAndSetSeq(clientMsgID: String, msgSeq: Int) {
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.msgSeq = msgSeq
        }
    }

    suspend fun updateResendMessage(clientMsgID: String, newClientMsgId: String, msgSeq: Int) {
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.clientMsgID = newClientMsgId
            msg?.msgSeq = msgSeq
            msg?.sendSuccess = true
            msg?.sendStamp = newClientMsgId.split("-").last().toLong()
        }
    }

    suspend fun insertMessage(msg: MessageEntity) {
        realm.write {
            val msgExit = query<MessageEntity>(
                query = "clientMsgID == $0", msg.clientMsgID,
            ).first().find()
//            Log.d("RealmTAG", "要插入的 Msg：${msg.msgBody}， 查找到缓存 --->> ${msgExit?.msgBody}")
            if (msgExit == null) {
                Log.d("RealmTAG", "未查找到缓存，即将插入的 Msg：${msg.msgBody}")
                copyToRealm(msg)
            }
        }
    }

    suspend fun deleteMessage(id: ObjectId) {
        realm.write {
            val msg = query<MessageEntity>(query = "_id == $0", id).first().find()
            try {
                msg?.let { delete(it) }
            } catch (e: Exception) {
                Log.d("IMLocalRepository", "${e.message}")
            }
        }
    }
}