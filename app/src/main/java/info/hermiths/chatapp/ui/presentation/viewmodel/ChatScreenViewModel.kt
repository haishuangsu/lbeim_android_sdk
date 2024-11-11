package info.hermiths.chatapp.ui.presentation.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tinder.scarlet.Message
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.WebSocket.Event.OnConnectionClosed
import com.tinder.scarlet.WebSocket.Event.OnConnectionClosing
import com.tinder.scarlet.WebSocket.Event.OnConnectionFailed
import com.tinder.scarlet.WebSocket.Event.OnConnectionOpened
import com.tinder.scarlet.WebSocket.Event.OnMessageReceived
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import info.hermiths.chatapp.BuildConfig
import info.hermiths.chatapp.data.local.IMLocalRepository
import info.hermiths.chatapp.data.remote.LbeConfigRepository
import info.hermiths.chatapp.data.remote.LbeImRepository
import info.hermiths.chatapp.data.remote.UploadRepository
import info.hermiths.chatapp.model.MediaMessage
import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.proto.IMMsg
import info.hermiths.chatapp.model.req.CompleteMultiPartUploadReq
import info.hermiths.chatapp.model.req.ConfigBody
import info.hermiths.chatapp.model.req.HistoryBody
import info.hermiths.chatapp.model.req.InitMultiPartUploadBody
import info.hermiths.chatapp.model.req.MsgBody
import info.hermiths.chatapp.model.req.Pagination
import info.hermiths.chatapp.model.req.Part
import info.hermiths.chatapp.model.req.SeqCondition
import info.hermiths.chatapp.model.req.SessionBody
import info.hermiths.chatapp.model.req.SessionListReq
import info.hermiths.chatapp.model.resp.History
import info.hermiths.chatapp.model.resp.MediaSource
import info.hermiths.chatapp.model.resp.Resource
import info.hermiths.chatapp.model.resp.SessionEntry
import info.hermiths.chatapp.model.resp.Thumbnail
import info.hermiths.chatapp.service.ChatService
import info.hermiths.chatapp.service.DynamicHeaderUrlRequestFactory
import info.hermiths.chatapp.service.RetrofitInstance
import info.hermiths.chatapp.ui.presentation.screen.ChatScreenUiState
import info.hermiths.chatapp.utils.Converts.entityToSendBody
import info.hermiths.chatapp.utils.Converts.protoToEntity
import info.hermiths.chatapp.utils.Converts.sendBodyToEntity
import info.hermiths.chatapp.utils.TimeUtils.timeStampGen
import info.hermiths.chatapp.utils.UUIDUtils.uuidGen
import info.hermiths.chatapp.utils.UploadBigFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest


enum class ConnectionStatus {
    NOT_STARTED, OPENED, CLOSED, CONNECTING, CLOSING, FAILED, RECEIVED
}

class ChatScreenViewModel : ViewModel() {

    companion object {
        private const val TAG = "IM Websocket"
        private const val REALM = "RealmTAG"
        private const val UPLOAD = "IM UPLOAD"
        const val FILESELECT = "File Select"
        const val IMAGEENCRYPTION = "Image Encryption"
        var lbeSign = BuildConfig.lbeSign
        var uid = "c-4385obtijcnd"
        var wssHost = ""
        var oss = ""
        var lbeToken = ""
        var lbeSession = ""
        var seq: Int = 0
        var sessionList: MutableList<SessionEntry> = mutableListOf()
        var currentSession: SessionEntry? = null
        var currentSessionIndex = 0
        var currentSessionTotalPages = 0
        var showPageSize = 20
        var currentPage = 0
        var remoteLastMsgType = -1
        var nickId: String = ""
        var nickName: String = ""
    }

    private val _uiState = MutableLiveData(ChatScreenUiState())
    val uiState: LiveData<ChatScreenUiState> = _uiState

    private val _inputMsg = MutableLiveData("")
    val inputMsg: LiveData<String> = _inputMsg

    private var chatService: ChatService? = null
    var lazyListState: LazyListState? = null

    init {
        // TODO
        // prepare()
    }

    fun setNickId(nid: String) {
        if (nid.isEmpty()) {
            return
        }
        _uiState.postValue(_uiState.value?.copy(nickId = nid))
        nickId = nid
        nickName = nid
        prepare()
    }

    private fun prepare() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                fetchConfig()
                createSession()
                viewModelScope.launch(Dispatchers.IO) {
                    fetchSessionList()
                    observerConnection()
                }
            } catch (e: Exception) {
                println("Prepare error: $e")
            }
        }
    }

    private suspend fun fetchConfig() {
        val config = LbeConfigRepository.fetchConfig(lbeSign, ConfigBody(0, 1))
        wssHost = config.data.ws[0]
        RetrofitInstance.IM_URL = config.data.rest[0]
        RetrofitInstance.UPLOAD_BASE_URL = config.data.oss[0]
    }

    private suspend fun createSession() {
        val session = LbeImRepository.createSession(
            lbeSign, SessionBody(
                extraInfo = "", headIcon = "", nickId = nickId, nickName = nickName, uid = ""
            )
        )
        lbeToken = session.data.token
        lbeSession = session.data.sessionId
        uid = session.data.uid
    }

    private suspend fun fetchSessionList() {
        val sessionListRep = LbeImRepository.fetchSessionList(
            lbeToken = lbeToken, body = SessionListReq(
                pagination = Pagination(
                    pageNumber = 1, showNumber = 1000
                ), sessionType = 2
            )
        )
        sessionList.addAll(sessionListRep.data.sessionList)
        currentSession = sessionList[currentSessionIndex]
        seq = currentSession?.latestMsg?.msgSeq ?: 0
        remoteLastMsgType = currentSession?.latestMsg?.msgType ?: 0
        updateTotalPages()
        filterLocalMessages(needScrollEnd = true)
    }

    // TODO 分页只改变 currentPage;
    // TODO 跨会话改变 currentSession: currentPage > currentSessionTotalPages
    suspend fun filterLocalMessages(
        sid: String = currentSession?.sessionId ?: "",
        send: Boolean = false,
        needScrollEnd: Boolean = false, // send needScrollEnd
    ) {
        Log.d(
            REALM,
            "filterLocalMessages ---->>> currentSessionTotalPages: $currentSessionTotalPages, currentPage: $currentPage, seq: $seq"
        )

        if ((currentSessionTotalPages != 0 && currentPage > currentSessionTotalPages) || currentPage < 1) return

        var cacheMessages = IMLocalRepository.filterMessages(sid)

//        Log.d(
//            REALMTAG,
//            "find all msg filter ---->>> ${cacheMessages.map { m -> "(${m.msgBody},${m.msgSeq})" }}"
//        )
        Log.d(REALM, "cache size: ${cacheMessages.size} |  remote lastSeq: $seq ")

        // sync
        if (cacheMessages.size < seq && remoteLastMsgType != 0) { // || cacheMessages.last().msgSeq < seq && remoteLastMsgType != 0) {
            fetchHistoryAndSync()
        }

        // query newest cache
        cacheMessages = IMLocalRepository.filterMessages(sid)
        currentSessionTotalPages = cacheMessages.size / showPageSize
        val yu = cacheMessages.size % showPageSize
        Log.d(REALM, "分页总页数: $currentSessionTotalPages, 取余: $yu")

        if (send) currentPage = currentSessionTotalPages

        val subList = if (currentPage == 1 && yu != 0) {
            val start = Math.max((currentPage - 1) * showPageSize, 0)
            val end = Math.min(currentPage * showPageSize + yu, cacheMessages.size)
            Log.d(REALM, "最后一页 --->>> currentPage: $currentPage, start: $start, end: $end")
            cacheMessages.subList(start, end)
        } else {
            val start = Math.max(
                cacheMessages.size - showPageSize * (currentSessionTotalPages - (currentPage - 1)),
                0
            )
            val end = Math.min(start + showPageSize, cacheMessages.size)
            Log.d(REALM, "非最后一页 --->>> currentPage: $currentPage, start: $start, end: $end")
            cacheMessages.subList(start, end)
        }

        viewModelScope.launch(Dispatchers.Main) {
            val messages = uiState.value?.messages?.toMutableList()
            if (!send) {
                messages?.addAll(0, subList)
            } else {
                // messages?.add(subList.last()) 失败消息重发时，旧消息没 clear
                messages?.clear()
                messages?.addAll(subList)
            }
            _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
            if (needScrollEnd) {
                scrollTo(messages?.size ?: 0)
            }
        }
    }

    private fun updateTotalPages() {
        val cacheMessages = IMLocalRepository.filterMessages(lbeSession)
        Log.d(REALM, "update total pages cacheMessages size---->>> ${cacheMessages.size}")
        if (cacheMessages.isNotEmpty()) {
            currentSessionTotalPages = Math.max(cacheMessages.size / showPageSize, 1)
            currentPage = currentSessionTotalPages
            Log.d(REALM, "update total pages ---->>> $currentSessionTotalPages")
        } else {
            currentPage = 1
            currentSessionTotalPages = 1
        }
    }

    private fun scrollTo(index: Int) {
        Log.d(REALM, "scrollToEnd： $index")
        lazyListState?.requestScrollToItem(index)
    }


    private suspend fun fetchHistoryAndSync(): History {
        val history = LbeImRepository.fetchHistory(
            BuildConfig.lbeSign, lbeToken, HistoryBody(
                sessionId = currentSession?.sessionId ?: "",
                seqCondition = SeqCondition(startSeq = 0, endSeq = 1000)
            )
        )
        Log.d(REALM, "History sync")
        if (history.data.content.isNotEmpty()) {
            seq = history.data.content.last().msgSeq
            for (content in history.data.content) {
                if (content.msgType == 1 || content.msgType == 2 || content.msgType == 3) {
                    val entity = MessageEntity().apply {
                        sessionId = content.sessionId
                        senderUid = content.senderUid
                        msgBody = content.msgBody
                        clientMsgID = content.clientMsgID
                        msgType = content.msgType
                        sendStamp = content.clientMsgID.split("-").last().toLong()
                        msgSeq = content.msgSeq
                    }
                    IMLocalRepository.insertMessage(entity)
                }
            }
        }
        return history
    }

    @SuppressLint("CheckResult")
    private fun observerConnection() {
        chatService = Scarlet.Builder().webSocketFactory(
            OkHttpClient.Builder().build().newWebSocketFactory(
                DynamicHeaderUrlRequestFactory(
                    url = wssHost, lbeToken = lbeToken, lbeSession = lbeSession,
                )
            )
        ).addMessageAdapterFactory(ProtobufMessageAdapter.Factory())
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory()).build().create<ChatService>()

        Log.d(TAG, "Observing Connection")
        updateConnectionStatus(ConnectionStatus.CONNECTING)
        chatService?.observeConnection()?.subscribe({ response ->
            Log.d(TAG, response.toString())
            onResponseReceived(response)
        }, { error ->
            error.localizedMessage?.let { Log.e(TAG, it) }
        })
    }

    private fun onResponseReceived(response: WebSocket.Event) {
        when (response) {
            is OnConnectionOpened<*> -> updateConnectionStatus(ConnectionStatus.OPENED)

            is OnConnectionClosed -> updateConnectionStatus(ConnectionStatus.CLOSED)

            is OnConnectionClosing -> updateConnectionStatus(ConnectionStatus.CLOSING)

            is OnConnectionFailed -> updateConnectionStatus(ConnectionStatus.FAILED)

            is OnMessageReceived -> handleOnMessageReceived(response.message)
        }
    }

    private fun handleOnMessageReceived(message: Message) {
        Log.d(TAG, "handleOnMessageReceived: $message")
        try {
            val value = (message as Message.Bytes).value
            viewModelScope.launch {
                val msgEntity = IMMsg.MsgEntityToFrontEnd.parseFrom(value)
                Log.d(TAG, "handleOnMessageReceived protobuf bytes: $msgEntity")
                if (msgEntity.msgBody.sessionId.isEmpty() || msgEntity.msgBody.clientMsgID.isEmpty()) {
                    return@launch
                }
                val receivedReq = msgEntity.msgBody.msgSeq
                if (receivedReq - seq > 2) {
                    fetchHistoryAndSync()
                } else {
                    seq = receivedReq
                    remoteLastMsgType = when (msgEntity.msgBody.msgType) {
                        IMMsg.MsgType.JoinServer -> 0
                        IMMsg.MsgType.TextMsgType -> 1
                        IMMsg.MsgType.ImgMsgType -> 2
                        IMMsg.MsgType.VideoMsgType -> 3
                        IMMsg.MsgType.CreateSessionMsgType -> 4
                        else -> 9
                    }
                }
                Log.d(REALM, "收到消息 --->> seq: $seq, remoteLastMsgType: $remoteLastMsgType")
                viewModelScope.launch {
                    val entity = protoToEntity(msgEntity.msgBody)
                    IMLocalRepository.insertMessage(entity)
                    if (entity.senderUid != uid) {
                        attachMsgToUI(entity)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleOnMessageReceived: ", e)
        }
    }

    private fun updateConnectionStatus(connectionStatus: ConnectionStatus) {
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.postValue(_uiState.value?.copy(connectionStatus = connectionStatus))
        }
    }

    fun onMessageChange(message: String) {
        _inputMsg.postValue(message)
    }

    fun sendMessageFromInput(messageSent: () -> Unit) {
        if ((_inputMsg.value ?: "").isEmpty()) return

        send(messageSent = messageSent, msg = _inputMsg.value ?: "", msgType = 1)
    }

    private fun senMessageFromMedia(msg: String, msgType: Int) {
        send(messageSent = {}, msg = msg, msgType = msgType)
    }

    private fun send(messageSent: () -> Unit, msg: String, msgType: Int) {
        val uuid = uuidGen()
        val timeStamp = timeStampGen()
        val clientMsgId = "${uuid}-${timeStamp}"
        val body = MsgBody(
            msgBody = msg, msgSeq = seq, msgType = msgType, clientMsgId = clientMsgId, source = 100
        )
        val entity = sendBodyToEntity(body)
        viewModelScope.launch(Dispatchers.IO) {
            IMLocalRepository.insertMessage(entity)
            updateTotalPages()
            try {
                val senMsg = LbeImRepository.sendMsg(
                    lbeToken = lbeToken, lbeSession = lbeSession, body
                )
                seq = senMsg.data.msgReq
                remoteLastMsgType = msgType
                IMLocalRepository.findMsgAndSetSeq(clientMsgId, seq)
            } catch (e: Exception) {
                println("send error -->> $e")
                IMLocalRepository.findMsgAndSetStatus(clientMsgId, false)
            } finally {
                filterLocalMessages(send = true, needScrollEnd = true)
                updateTotalPages()
                viewModelScope.launch(Dispatchers.Main) {
                    messageSent()
                    clearInput()
                }
            }
        }
    }

    fun reSendMessage(clientMsgId: String) {
        val entity = IMLocalRepository.findMsgByClientMsgId(clientMsgId)
        var newClientMsgId = ""
        if (entity != null) {
            val list = entity.clientMsgID.split("-").toMutableList()
            list.removeLast()
            list.add(timeStampGen().toString())
            newClientMsgId = list.joinToString(separator = "-")
            val body = entityToSendBody(entity, newClientMsgId)
            println("reSend ====>>> old: ${entity.clientMsgID}, new: $newClientMsgId")
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val senMsg = LbeImRepository.sendMsg(
                        lbeToken = lbeToken, lbeSession = lbeSession, body
                    )
                    seq = senMsg.data.msgReq
                    IMLocalRepository.updateResendMessage(clientMsgId, newClientMsgId, seq)
                } catch (e: Exception) {
                    println("send error -->> $e")
                } finally {
                    filterLocalMessages(send = true, needScrollEnd = true)
                }
            }
        }
    }

    fun upload(mediaMessage: MediaMessage) {
        Log.d(UPLOAD, "upload file size---->>> ${mediaMessage.file.length()}")
        if (mediaMessage.file.length() > UploadBigFileUtils.defaultChunkSize) {
            bigFileUpload(mediaMessage)
        } else {
            singleUpload(mediaMessage)
        }
    }

    private fun singleUpload(mediaMessage: MediaMessage) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rep = UploadRepository.singleUpload(
                    file = MultipartBody.Part.createFormData(
                        "file", mediaMessage.file.name, mediaMessage.file.asRequestBody()
                    ), signType = 1
                )
                Log.d(UPLOAD, "single upload ---->>> ${rep.data.paths[0]}")
                val mediaSource = MediaSource(
                    thumbnail = Thumbnail(url = "", key = ""), resource = Resource(
                        url = rep.data.paths[0].url, key = rep.data.paths[0].key
                    )
                )
                senMessageFromMedia(msg = Gson().toJson(mediaSource), msgType = 2)
            } catch (e: Exception) {
                Log.d(UPLOAD, "Single upload error --->> $e")
            }
        }
    }

    private fun bigFileUpload(mediaMessage: MediaMessage) {
        try {
            val start = System.currentTimeMillis()
            Log.d(
                UPLOAD,
                "Big file upload ---->>> fileName: ${mediaMessage.file.name}, Fs hash: ${mediaMessage.file.hashCode()}, split start: $start"
            )
            UploadBigFileUtils.splitFile(mediaMessage.file, UploadBigFileUtils.defaultChunkSize)
            val end = System.currentTimeMillis()
            Log.d(UPLOAD, "split end: $end, diff: ${end - start}")
            viewModelScope.launch(Dispatchers.IO) {
                val initRep = UploadRepository.initMultiPartUpload(
                    body = InitMultiPartUploadBody(
                        size = mediaMessage.file.length(),
                        name = mediaMessage.file.name,
                        contentType = ""
                    )
                )
                Log.d(UPLOAD, "init multi upload --->>> $initRep")
                val completeMultiPartUploadReq = CompleteMultiPartUploadReq(
                    uploadId = initRep.data.uploadId,
                    name = mediaMessage.file.name,
                    part = mutableListOf()
                )
                val buffers = UploadBigFileUtils.blocks[mediaMessage.file.hashCode()]
                if (buffers != null) {
                    var index = 1
                    for (buffer in buffers) {
                        val md5 = MessageDigest.getInstance("MD5")
                        val sign = md5.digest(buffer.array())
                        val hexString = sign.joinToString("") { "%02x".format(it) }
                        Log.d(
                            UPLOAD,
                            "split chunk size: ${buffer.array().size}, hexString: $hexString"
                        )
                        completeMultiPartUploadReq.part.add(
                            Part(
                                partNumber = index, etag = hexString
                            )
                        )
                        val bodyFromBuffer = buffer.array().toRequestBody(
                            contentType = "application/octet-stream".toMediaTypeOrNull(),
                            byteCount = buffer.array().size
                        )
                        UploadRepository.uploadBinary(
                            url = initRep.data.node[buffers.indexOf(buffer)].url, bodyFromBuffer
                        )
                        index++
                    }
                }
                Log.d(UPLOAD, "iter --->> $completeMultiPartUploadReq")
                val mergeUpload = UploadRepository.completeMultiPartUpload(
                    body = completeMultiPartUploadReq
                )
                Log.d(UPLOAD, "BigFileUpload success ---> ${mergeUpload.data.location}")
                val mediaSource = MediaSource(
                    thumbnail = Thumbnail(url = "", key = ""), resource = Resource(
                        url = mergeUpload.data.location, key = ""
                    )
                )
                senMessageFromMedia(msg = Gson().toJson(mediaSource), msgType = 3)
            }
        } catch (e: Exception) {
            Log.d(UPLOAD, "Big file upload error --->>> $e")
        }
    }

    private fun attachMsgToUI(message: MessageEntity) {
        Log.d(TAG, "attachMsgToUI: $message")
        val messages = uiState.value?.messages?.toMutableList()
        messages?.add(message)
        _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
    }

    private fun clearInput() {
        viewModelScope.launch {
            delay(50)
            _inputMsg.postValue("")
        }
    }
}