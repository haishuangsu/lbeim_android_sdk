package info.hermiths.chatapp.data

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

    suspend fun findMsgAndSetStatus(clientMsgID: String, success: Boolean) {
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.sendSuccess = success
        }
    }

    suspend fun insertMessage(msg: MessageEntity) {
        realm.write {
            val msgExit = query<MessageEntity>(
                query = "clientMsgID == $0", msg.clientMsgID,
            ).first().find()
            Log.d("RealmTAG", "插入前查找到Session --->> ${msgExit.toString()}")
            if (msgExit == null) {
                copyToRealm(msg)
            }
        }
    }

    suspend fun updateClientMsgID(msg: MessageEntity, clientMsgID: String) {
        realm.write {
            val queriedMsg =
                query<MessageEntity>(query = "clientMsgID == $0", msg.clientMsgID).first().find()
            queriedMsg?.clientMsgID = clientMsgID
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