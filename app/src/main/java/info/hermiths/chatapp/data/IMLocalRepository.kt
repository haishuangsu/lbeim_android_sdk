package info.hermiths.chatapp.data

import android.util.Log
import info.hermiths.chatapp.model.MessageEntity
import io.realm.kotlin.ext.query
import org.mongodb.kbson.ObjectId

object IMLocalRepository {
    private val realm = RealmInstance.realm

    fun findMessages(): List<MessageEntity> {
        return realm.query<MessageEntity>().find()
    }

    fun filterMessages(filter: String, args: String): List<MessageEntity> {
        // (query = "name CONTAINS[c] $0", args)
        return realm.query<MessageEntity>(query = filter, args).find()
    }

    suspend fun insertMessage(msg: MessageEntity) {
        realm.write {
            val msgExit =
                query<MessageEntity>(query = "clientMsgID == $0", msg.clientMsgID).first().find()
            Log.d("RealmTAG", "插入前查找 --->> ${msgExit.toString()}")
            if (msgExit == null) {
                copyToRealm(msg)
            }
        }
    }

    suspend fun updateMessage(msg: MessageEntity, clientMsgID: String) {
        realm.write {
            val queriedPerson =
                query<MessageEntity>(query = "clientMsgID == $0", msg.clientMsgID).first().find()
            queriedPerson?.clientMsgID = clientMsgID
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