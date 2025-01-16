package com.lbe.imsdk.data.local

import android.util.Log
import com.lbe.imsdk.model.MessageEntity
import com.lbe.imsdk.model.UploadTask
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import org.mongodb.kbson.ObjectId

object IMLocalRepository {
    private val realm = RealmInstance.realm

    fun filterMessages(sessionId: String): List<MessageEntity> {
        return realm.query<MessageEntity>(
            query = "sessionId == $0", sessionId
        ).sort("sendTime", Sort.ASCENDING).find()
    }

    fun findMsgByClientMsgId(clientMsgID: String): MessageEntity? {
        return realm.query<MessageEntity>(
            query = "clientMsgID == $0", clientMsgID
        ).first().find()
    }

    fun findAllMediaMessages(sessionId: String): List<MessageEntity> {
        return realm.query<MessageEntity>(
            query = "sessionId == $0 AND (msgType == 2 || msgType == 3)", sessionId
        ).sort("sendTime", Sort.ASCENDING).find()
    }

    fun findAllPendingUploadMediaMessages(sessionId: String): List<MessageEntity> {
        return realm.query<MessageEntity>(
            query = "sessionId == $0 AND (msgType == 2 || msgType == 3) AND pendingUpload == true",
            sessionId
        ).sort("sendTime", Sort.ASCENDING).find()
    }

    suspend fun findMediaMsgAndUpdateBody(clientMsgID: String, msgBody: String) {
        Log.d(ChatScreenViewModel.REALM, "发送 Media 消息 --- $clientMsgID 更新 Body： $msgBody")
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.msgBody = msgBody
        }
    }

    suspend fun findMediaMsgAndUpdateProgress(clientMsgID: String, uploadTask: UploadTask?) {
        Log.d(
            ChatScreenViewModel.REALM,
            "发送 Media 消息 --- $clientMsgID 更新上传进度： ${uploadTask?.progress}, executeIndex: ${uploadTask?.executeIndex}"
        )
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.uploadTask = uploadTask
            msg?.pendingUpload = true
        }
    }

    suspend fun findMediaMsgSetUploadContinue(clientMsgID: String) {
        Log.d(
            ChatScreenViewModel.REALM, "发送 Media 消息 --- $clientMsgID ---->>> 大文件上传续传"
        )
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.pendingUpload = false
        }
    }

    suspend fun findMediaMsgUpdateCanPending(clientMsgID: String) {
        Log.d(
            ChatScreenViewModel.REALM, "发送 Media 消息 --- $clientMsgID ---->>> 大文件上传续传"
        )
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.canPending = true
        }
    }

    suspend fun findMsgAndSetStatus(clientMsgID: String, success: Boolean) {
        Log.d(ChatScreenViewModel.REALM, "发送消息 --- $clientMsgID 更新状态： $success")
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
            msg?.sendTime = newClientMsgId.split("-").last().toLong()
            msg?.timestamp = RealmInstant.now()
        }
    }

    suspend fun insertMessage(msg: MessageEntity) {
        realm.write {
            val msgExit = query<MessageEntity>(
                query = "clientMsgID == $0", msg.clientMsgID,
            ).first().find()
            Log.d("RealmTAG", "要插入的 Msg：$msg， 查找到缓存 --->> ${msgExit?.msgBody}")
            if (msgExit == null) {
                Log.d("RealmTAG", "未查找到缓存，即将插入的 Msg：${msg.msgBody}")
                copyToRealm(msg)
            }
        }
    }

    suspend fun findMsgAndMarkMeRead(clientMsgID: String) {
        Log.d(ChatScreenViewModel.REALM, "我标记消息已读 --- $clientMsgID ")
        realm.write {
            val msg = query<MessageEntity>(
                query = "clientMsgID == $0", clientMsgID
            ).first().find()
            msg?.readed = true
        }
    }

    suspend fun findMsgAndMarkCsRead(sessionId: String, seq: Int) {
        Log.d(ChatScreenViewModel.REALM, "客服标记消息已读 --- $sessionId, $seq")
        realm.write {
            val msg = query<MessageEntity>(
                query = "sessionId == $0 AND msgSeq == $1", sessionId, seq
            ).first().find()
            msg?.readed = true
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