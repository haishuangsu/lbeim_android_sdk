package info.hermiths.chatapp.ui.presentation.viewmodel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
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
import info.hermiths.chatapp.data.local.IMLocalRepository.findMediaMsgAndUpdateProgress
import info.hermiths.chatapp.data.remote.LbeConfigRepository
import info.hermiths.chatapp.data.remote.LbeImRepository
import info.hermiths.chatapp.data.remote.UploadRepository
import info.hermiths.chatapp.model.LocalMediaFile
import info.hermiths.chatapp.model.MediaMessage
import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.UploadTask
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
import info.hermiths.chatapp.model.resp.InitMultiPartUploadRep
import info.hermiths.chatapp.model.resp.MediaSource
import info.hermiths.chatapp.model.resp.Resource
import info.hermiths.chatapp.model.resp.SessionEntry
import info.hermiths.chatapp.model.resp.SingleUploadRep
import info.hermiths.chatapp.model.resp.Thumbnail
import info.hermiths.chatapp.service.ChatService
import info.hermiths.chatapp.service.DynamicHeaderUrlRequestFactory
import info.hermiths.chatapp.service.RetrofitInstance
import info.hermiths.chatapp.ui.presentation.components.ProgressRequestBody
import info.hermiths.chatapp.ui.presentation.components.ProgressRequestBody.Companion.toRequestBody
import info.hermiths.chatapp.ui.presentation.screen.ChatScreenUiState
import info.hermiths.chatapp.utils.Converts.entityToMediaSendBody
import info.hermiths.chatapp.utils.Converts.entityToSendBody
import info.hermiths.chatapp.utils.Converts.protoToEntity
import info.hermiths.chatapp.utils.Converts.sendBodyToEntity
import info.hermiths.chatapp.utils.TimeUtils.timeStampGen
import info.hermiths.chatapp.utils.UUIDUtils.uuidGen
import info.hermiths.chatapp.utils.UploadBigFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest

enum class ConnectionStatus {
    NOT_STARTED, OPENED, CLOSED, CONNECTING, CLOSING, FAILED, RECEIVED
}

class ChatScreenViewModel : ViewModel() {

    companion object {
        private const val TAG = "IM Websocket"
        const val REALM = "RealmTAG"
        const val UPLOAD = "IM UPLOAD"
        const val FILESELECT = "File Select"
        const val IMAGEENCRYPTION = "Image Encryption"
        const val CONTINUE_UPLOAD = "CONTINUE_UPLOAD"
        var lbeSign = BuildConfig.lbeSign
        var uid = "c-43ro83fgre8a"
        var wssHost = ""
        var lbeToken = ""
        var lbeSession = ""
        var seq: Int = 0
        var sessionList: MutableList<SessionEntry> = mutableListOf()
        var currentSession: SessionEntry? = null
        var currentSessionIndex = 0
        var currentSessionTotalPages = 1
        var showPageSize = 20
        var currentPage = 1
        var remoteLastMsgType = -1
        var nickId: String = ""
        var nickName: String = ""
        var lbeIdentity: String = "" // 42nz10y3hhah
        var progressList: MutableMap<String, MutableStateFlow<Float>> = mutableMapOf()
        var uploadThumbs: MutableMap<String, MutableStateFlow<Bitmap>> = mutableMapOf()
    }

    private val jobs: MutableMap<String, Job> = mutableMapOf()
    val mergeMultiUploadReqQueue: MutableMap<String, CompleteMultiPartUploadReq> = mutableMapOf()
    val uploadTasks: MutableMap<String, UploadTask> = mutableMapOf()

    private val _uiState = MutableLiveData(ChatScreenUiState())
    val uiState: LiveData<ChatScreenUiState> = _uiState

//    private val _messages = MutableStateFlow<List<MessageEntity>>(mutableListOf())
//    val messageList = _messages

    private val _inputMsg = MutableLiveData("")
    val inputMsg: LiveData<String> = _inputMsg

    private var chatService: ChatService? = null
    var lazyListState: LazyListState? = null

    init {
        // TODO
        // prepare()
        // testOfflineTakeByCache()
    }

    private fun testOfflineTakeByCache() {
        currentSession = SessionEntry(sessionId = "cn-43ro83fqqzm2", latestMsg = null)
        lbeSession = "cn-43ro83fqqzm2"
        syncPageInfo()
        viewModelScope.launch(Dispatchers.IO) {
            filterLocalMessages(needScrollEnd = true)
        }
    }

    fun setNickId(nid: String, nName: String, identity: String) {
        if (nid.isEmpty()) {
            return
        }
        _uiState.postValue(_uiState.value?.copy(login = true))
        nickId = nid
        nickName = nName
        lbeIdentity = identity
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
        try {
            val config = LbeConfigRepository.fetchConfig(lbeSign, lbeIdentity, ConfigBody(0, 1))
            wssHost = config.data.ws[0]
            RetrofitInstance.IM_URL = config.data.rest[0]
            RetrofitInstance.UPLOAD_BASE_URL = config.data.oss[0]
        } catch (e: Exception) {
            println("Fetch config error: $e")
        }
    }

    private suspend fun createSession() {
        try {
            val session = LbeImRepository.createSession(
                lbeSign, lbeIdentity = lbeIdentity, SessionBody(
                    extraInfo = "", headIcon = "", nickId = nickId, nickName = nickName, uid = ""
                )
            )
            lbeToken = session.data.token
            lbeSession = session.data.sessionId
            uid = session.data.uid
        } catch (e: Exception) {
            println("Create session error: $e")
        }
    }

    private suspend fun fetchSessionList() {
        try {
            val sessionListRep = LbeImRepository.fetchSessionList(
                lbeToken = lbeToken, lbeIdentity = lbeIdentity, body = SessionListReq(
                    pagination = Pagination(
                        pageNumber = 1, showNumber = 1000
                    ), sessionType = 2
                )
            )
            sessionList.addAll(sessionListRep.data.sessionList)
            currentSession = sessionList[currentSessionIndex]
            seq = currentSession?.latestMsg?.msgSeq ?: 0
            remoteLastMsgType = currentSession?.latestMsg?.msgType ?: 0
            syncPageInfo()
            filterLocalMessages(needScrollEnd = true)
            // TODO 查缓存同步未上传完列表, progressList
            syncPendingJobs()
        } catch (e: Exception) {
            println("Fetch session list error: $e")
        }
    }

    private fun syncPendingJobs() {
        val pendingCache =
            IMLocalRepository.findAllPendingUploadMediaMessages(currentSession?.sessionId ?: "")
        Log.d(
            REALM,
            "PendingJobs --->>> ${pendingCache.map { cache -> "${cache.clientMsgID} || ${cache.uploadTask?.progress} " }}"
        )
        for (pending in pendingCache) {
            progressList[pending.clientMsgID] =
                MutableStateFlow(pending.uploadTask?.progress ?: 0.0f)
        }
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

        Log.d(
            REALM,
            "cache size: ${cacheMessages.size} |  remote lastSeq: $seq , remoteLastMsgType: $remoteLastMsgType"
        )

        // sync
        if (cacheMessages.size < seq) { //&& remoteLastMsgType != 0) { // || cacheMessages.last().msgSeq < seq && remoteLastMsgType != 0) {
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

//                _messages.update { currentState ->
//                    val news = currentState.toMutableList()
//                    news.addAll(0, subList)
//                    news
//                }
            } else {
                // messages?.add(subList.last()) 失败消息重发时，旧消息没 clear
                messages?.clear()
                messages?.addAll(subList)

//                _messages.update { currentState ->
//                    val news = currentState.toMutableList()
//                    news.clear()
//                    news.addAll(subList)
//                    news
//                }
            }
            _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
            Log.d(REALM, "分页后 messageList size --->> ${messages?.size}")
            if (needScrollEnd) {
                scrollTo(messages?.size ?: 0)
            }
        }
    }

    private fun syncPageInfo() {
        val cacheMessages = IMLocalRepository.filterMessages(lbeSession)
        Log.d(REALM, "update total pages cacheMessages size---->>> ${cacheMessages.size}")
        if (cacheMessages.isNotEmpty()) {
            currentSessionTotalPages = Math.max(cacheMessages.size / showPageSize, 1)
            currentPage = currentSessionTotalPages
            Log.d(
                REALM,
                "update total pages ---->>> currentSessionTotalPages: $currentSessionTotalPages, currentPage: $currentPage"
            )
        } else {
            currentPage = 1
            currentSessionTotalPages = 1
        }
    }

    private fun scrollTo(index: Int) {
        Log.d(REALM, "scrollToEnd： $index")
        lazyListState?.requestScrollToItem(index)
    }


    private suspend fun fetchHistoryAndSync() {
        try {
            val history = LbeImRepository.fetchHistory(
                lbeSign = BuildConfig.lbeSign,
                lbeToken = lbeToken,
                lbeIdentity = lbeIdentity,
                body = HistoryBody(
                    sessionId = currentSession?.sessionId ?: "",
                    seqCondition = SeqCondition(startSeq = 0, endSeq = 230)
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
            syncPageInfo()
        } catch (e: Exception) {
            println("Fetch history error: $e")
        }
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

    fun sendMessageFromTextInput(messageSent: () -> Unit) {
        if ((_inputMsg.value ?: "").isEmpty()) return
        val sendBody = genMsgBody(type = 1, msgBody = _inputMsg.value ?: "")
        send(
            messageSent = messageSent,
            preSend = {
                insertCacheMaybeUpdateUI(sendBody, localFile = null, updateUI = false)
            },
            sendBody,
        )
    }

    private fun senMessageFromMedia(msgBody: MsgBody, preSend: () -> Unit) {
        send(messageSent = {}, preSend = preSend, msgBody = msgBody)
    }

    private fun send(messageSent: () -> Unit, preSend: () -> Unit, msgBody: MsgBody) {
        viewModelScope.launch(Dispatchers.IO) {
            preSend()
            try {
                val senMsg = LbeImRepository.sendMsg(
                    lbeToken = lbeToken,
                    lbeIdentity = lbeIdentity,
                    lbeSession = lbeSession,
                    body = msgBody
                )
                seq = senMsg.data.msgReq
                remoteLastMsgType = msgBody.msgType
                IMLocalRepository.findMsgAndSetSeq(msgBody.clientMsgId, seq)
            } catch (e: Exception) {
                println("send error -->> $e")
                IMLocalRepository.findMsgAndSetStatus(msgBody.clientMsgId, false)
            } finally {
                filterLocalMessages(send = true, needScrollEnd = true)
                syncPageInfo()
                viewModelScope.launch(Dispatchers.Main) {
                    messageSent()
                    clearInput()
                }
            }
        }
    }

    private fun genMsgBody(type: Int, msgBody: String = ""): MsgBody {
        val uuid = uuidGen()
        val timeStamp = timeStampGen()
        val clientMsgId = "${uuid}-${timeStamp}"
        val body = MsgBody(
            msgBody = msgBody,
            msgSeq = seq,
            msgType = type,
            clientMsgId = clientMsgId,
            source = 100,
            sendTime = timeStamp.toString()
        )
        return body
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
                        lbeToken = lbeToken,
                        lbeSession = lbeSession,
                        lbeIdentity = lbeIdentity,
                        body = body
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun upload(mediaMessage: MediaMessage) {
        Log.d(UPLOAD, "upload file size---->>> ${mediaMessage.file.length()}")
        if (mediaMessage.file.length() > UploadBigFileUtils.defaultChunkSize) {
            bigFileUpload(mediaMessage)
        } else {
            singleUpload(mediaMessage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun singleUpload(mediaMessage: MediaMessage) {
        val sendBody = genMsgBody(
            type = if (mediaMessage.isImage) 2 else 3,
        )

        val localFile = genLocalFile(mediaMessage)
        localFile.isBigFile = false

        val entity = insertCacheMaybeUpdateUI(
            sendBody = sendBody, localFile = localFile
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val thumbnailResp = uploadThumbnail(mediaMessage, sendBody.clientMsgId)
//                val thumbnailSource = MediaSource(
//                    isBigFile = false,
//                    width = mediaMessage.width,
//                    height = mediaMessage.height,
//                    thumbnail = Thumbnail(
//                        url = thumbnailResp.data.paths[0].url, key = thumbnailResp.data.paths[0].key
//                    ),
//                    resource = Resource(
//                        url = "", key = ""
//                    )
//                )
//                sendBody.msgBody = Gson().toJson(thumbnailSource)

                val rep = UploadRepository.singleUpload(
                    file = MultipartBody.Part.createFormData(
                        "file",
                        mediaMessage.file.name,
                        ProgressRequestBody(delegate = mediaMessage.file.asRequestBody(),
                            listener = { bytesWritten, contentLength ->
                                val progress = (1.0 * bytesWritten) / contentLength
                                Log.d(
                                    UPLOAD,
                                    "Single upload  ${mediaMessage.file.name} ---->>>  bytesWritten: $bytesWritten, $contentLength, progress: $progress"
                                )
                                val emitProgress = progressList[entity.clientMsgID]
                                if (emitProgress != null) {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        emitProgress.value = progress.toFloat()
                                    }

                                    if (emitProgress.value == 1.0f) {
                                        val uploadTask = UploadTask()
                                        uploadTask.progress = 1.0f
                                        viewModelScope.launch(Dispatchers.IO) {
                                            findMediaMsgAndUpdateProgress(
                                                entity.clientMsgID, uploadTask
                                            )
                                        }
                                    }
                                }
                            })
                    ), signType = if (mediaMessage.isImage) 2 else 1
                )
                Log.d(UPLOAD, "Single upload ---->>> ${rep.data.paths[0]}")
                val mediaSource = MediaSource(
                    isBigFile = false,
                    width = mediaMessage.width,
                    height = mediaMessage.height,
                    thumbnail = Thumbnail(
                        url = thumbnailResp.data.paths[0].url, key = thumbnailResp.data.paths[0].key
                    ),
                    resource = Resource(
                        url = rep.data.paths[0].url, key = rep.data.paths[0].key
                    )
                )
                sendBody.msgBody = Gson().toJson(mediaSource)
                senMessageFromMedia(sendBody, preSend = {
                    viewModelScope.launch(Dispatchers.IO) {
                        IMLocalRepository.findMediaMsgAndUpdateBody(
                            sendBody.clientMsgId, sendBody.msgBody
                        )
                    }
                })
            } catch (e: Exception) {
                Log.d(UPLOAD, "Single upload error --->> $e")
            }
        }
    }

    private fun insertCacheMaybeUpdateUI(
        sendBody: MsgBody, localFile: LocalMediaFile?, updateUI: Boolean = true
    ): MessageEntity {
        val entity = sendBodyToEntity(sendBody)
        if (localFile != null) {
            entity.localFile = localFile
        }
        viewModelScope.launch(Dispatchers.IO) {
            IMLocalRepository.insertMessage(entity)
            syncPageInfo()
            if (updateUI) {
                filterLocalMessages(send = true, needScrollEnd = true)
                progressList[entity.clientMsgID] = MutableStateFlow(0.0f)
            }
        }
        return entity
    }

    private fun genLocalFile(mediaMessage: MediaMessage): LocalMediaFile {
        val localFile = LocalMediaFile()
        localFile.fileName = mediaMessage.file.name
        localFile.path = mediaMessage.path
        localFile.size = mediaMessage.file.length()
        localFile.mimeType = mediaMessage.mime
        localFile.width = mediaMessage.width
        localFile.height = mediaMessage.height
        return localFile
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun bigFileUpload(mediaMessage: MediaMessage) {
        try {
            val sendBody = genMsgBody(
                type = if (mediaMessage.isImage) 2 else 3,
            )

            val localFile = genLocalFile(mediaMessage)
            localFile.isBigFile = true

            val entity = insertCacheMaybeUpdateUI(
                sendBody = sendBody, localFile = localFile
            )

            var executeIndex = 1
            val uploadTask = UploadTask()
            uploadTask.executeIndex = executeIndex
            uploadTasks[entity.clientMsgID] = uploadTask

            mergeMultiUploadReqQueue[entity.clientMsgID] = CompleteMultiPartUploadReq(
                uploadId = "", name = mediaMessage.file.name, part = mutableListOf()
            )

            val job = viewModelScope.launch(Dispatchers.IO) {
                val thumbnailResp = uploadThumbnail(mediaMessage, sendBody.clientMsgId)
                val thumbnailSource = MediaSource(
                    isBigFile = true,
                    width = mediaMessage.width,
                    height = mediaMessage.height,
                    thumbnail = Thumbnail(
                        url = thumbnailResp.data.paths[0].url, key = thumbnailResp.data.paths[0].key
                    ),
                    resource = Resource(
                        url = "", key = ""
                    )
                )
                sendBody.msgBody = Gson().toJson(thumbnailSource)
                IMLocalRepository.findMediaMsgAndUpdateBody(sendBody.clientMsgId, sendBody.msgBody)

                val initRep = UploadRepository.initMultiPartUpload(
                    body = InitMultiPartUploadBody(
                        size = mediaMessage.file.length(),
                        name = mediaMessage.file.name,
                        contentType = ""
                    )
                )
                Log.d(UPLOAD, "init multi upload --->>> $initRep")
                uploadTask.initTrunksRepJson = Gson().toJson(initRep)
                val start = System.currentTimeMillis()
                Log.d(
                    UPLOAD,
                    "Big file upload ---->>> fileName: ${mediaMessage.file.name}, Fs hash: ${mediaMessage.file.hashCode()}, split start: $start"
                )
                if (initRep.data.node.size > 1) {
                    UploadBigFileUtils.splitFile(
                        mediaMessage.file, UploadBigFileUtils.defaultChunkSize
                    )
                } else {
                    UploadBigFileUtils.splitFile(
                        mediaMessage.file, initRep.data.node[0].size
                    )
                }
                val end = System.currentTimeMillis()
                Log.d(UPLOAD, "split end: $end, diff: ${end - start}")

                val taskLength = initRep.data.node.size
                uploadTasks[entity.clientMsgID]?.taskLength = taskLength

                mergeMultiUploadReqQueue[entity.clientMsgID] = CompleteMultiPartUploadReq(
                    uploadId = initRep.data.uploadId,
                    name = mediaMessage.file.name,
                    part = mutableListOf()
                )
                val buffers = UploadBigFileUtils.blocks[mediaMessage.file.hashCode()]

                // TODO 1.拿到文件路径重切块 2.断点续传需要跳过已传的块 3.剩余块上传，拼接 merge requestBody
                if (buffers != null) {
                    var deltaSize = 0L
                    for (buffer in buffers) {
                        val md5 = MessageDigest.getInstance("MD5")
                        val sign = md5.digest(buffer.array())
                        val hexString = sign.joinToString("") { "%02x".format(it) }
                        Log.d(
                            UPLOAD,
                            "split chunk size: ${buffer.array().size}, hexString: $hexString"
                        )

                        val bodyFromBuffer =
                            ProgressRequestBody(delegate = buffer.array().toRequestBody(
                                contentType = "application/octet-stream".toMediaTypeOrNull(),
                                byteCount = buffer.array().size
                            ), listener = { bytesWritten, contentLength ->
                                val totalProgress =
                                    (1.0 * (deltaSize + bytesWritten)) / mediaMessage.file.length()
                                val progress = (1.0 * bytesWritten) / contentLength

//                                Log.d(
//                                    UPLOAD, "Split Upload progress ${
//                                        initRep.data.node[buffers.indexOf(
//                                            buffer
//                                        )].url
//                                    } ---->>>  split trunk bytesWritten: $bytesWritten, $contentLength, split trunk progress: $progress || Total progress: $totalProgress"
//                                )
                                val emitProgress = progressList[entity.clientMsgID]
                                if (emitProgress != null) {
                                    viewModelScope.launch(Dispatchers.Main) {
                                        emitProgress.value = totalProgress.toFloat()
                                    }


                                    if (emitProgress.value == 1.0f) {
                                        uploadTask.progress = 1.0f
                                        uploadTask.reqBodyJson =
                                            Gson().toJson(mergeMultiUploadReqQueue[entity.clientMsgID])
                                        viewModelScope.launch(Dispatchers.IO) {
                                            findMediaMsgAndUpdateProgress(
                                                entity.clientMsgID, uploadTask
                                            )
                                        }
                                    }
                                }
                            })
                        UploadRepository.uploadBinary(
                            url = initRep.data.node[buffers.indexOf(buffer)].url, bodyFromBuffer
                        )

                        mergeMultiUploadReqQueue[entity.clientMsgID]?.part?.add(
                            Part(
                                partNumber = executeIndex, etag = hexString
                            )
                        )
                        uploadTasks[entity.clientMsgID]?.executeIndex = executeIndex
                        deltaSize += buffer.array().size
                        executeIndex++
                    }
                }
                Log.d(UPLOAD, "iter --->> ${mergeMultiUploadReqQueue[entity.clientMsgID]}")
                val mergeUpload = mergeMultiUploadReqQueue[entity.clientMsgID]?.let {
                    UploadRepository.completeMultiPartUpload(
                        body = it
                    )
                }
                UploadBigFileUtils.releaseMemory(mediaMessage.file.hashCode())
                Log.d(UPLOAD, "BigFileUpload success ---> ${mergeUpload?.data?.location}")
                val mediaSource = MediaSource(
                    width = mediaMessage.width, height = mediaMessage.height, thumbnail = Thumbnail(
                        url = thumbnailResp.data.paths[0].url, key = thumbnailResp.data.paths[0].key
                    ), resource = Resource(
                        url = mergeUpload?.data?.location ?: "", key = ""
                    )
                )
                sendBody.msgBody = Gson().toJson(mediaSource)
                senMessageFromMedia(sendBody, preSend = {
                    viewModelScope.launch(Dispatchers.IO) {
                        IMLocalRepository.findMediaMsgAndUpdateBody(
                            sendBody.clientMsgId, sendBody.msgBody
                        )
                    }
                })
            }
            jobs[sendBody.clientMsgId] = job
        } catch (e: Exception) {
            Log.d(UPLOAD, "Big file upload error --->>> $e")
        }
    }

    fun continueSplitTrunksUpload(message: MessageEntity, file: File) {
        val job = viewModelScope.launch(Dispatchers.IO) {
            val uploadTask = message.uploadTask
            val newTask = UploadTask()
            if (uploadTask != null) {
                newTask.executeIndex = uploadTask.executeIndex
                newTask.taskLength = uploadTask.taskLength
                newTask.progress = uploadTask.progress
                newTask.reqBodyJson = uploadTask.reqBodyJson
                newTask.initTrunksRepJson = uploadTask.initTrunksRepJson
            }

            uploadTasks[message.clientMsgID] = newTask
            mergeMultiUploadReqQueue[message.clientMsgID] =
                Gson().fromJson(newTask.reqBodyJson, CompleteMultiPartUploadReq::class.java)

            var executeIndex = newTask.executeIndex + 1

            if (message.uploadTask?.taskLength!! > 1) {
                UploadBigFileUtils.splitFile(file, UploadBigFileUtils.defaultChunkSize)
            }
//                else {
//                    UploadBigFileUtils.splitFile(
//                        file, initRep.data.node[0].size
//                    )
//                }

            val buffers = UploadBigFileUtils.blocks[file.hashCode()]
            val initRep = Gson().fromJson(
                newTask.initTrunksRepJson, InitMultiPartUploadRep::class.java
            )

            if (buffers != null) {
                var deltaSize = 0L
                var tempIndex = 1
                for (buffer in buffers) {
                    if (tempIndex != -1 && tempIndex < executeIndex) {
                        Log.d(CONTINUE_UPLOAD, "tempIndex: $tempIndex, executeIndex: $executeIndex")
                        deltaSize += buffer.array().size
                        tempIndex++
                        continue
                    } else {
                        tempIndex = -1
                    }

                    val md5 = MessageDigest.getInstance("MD5")
                    val sign = md5.digest(buffer.array())
                    val hexString = sign.joinToString("") { "%02x".format(it) }
                    Log.d(
                        UPLOAD, "split chunk size: ${buffer.array().size}, hexString: $hexString"
                    )

                    val bodyFromBuffer =
                        ProgressRequestBody(delegate = buffer.array().toRequestBody(
                            contentType = "application/octet-stream".toMediaTypeOrNull(),
                            byteCount = buffer.array().size
                        ), listener = { bytesWritten, contentLength ->
                            val totalProgress = (1.0 * (deltaSize + bytesWritten)) / file.length()
                            val progress = (1.0 * bytesWritten) / contentLength

//                                Log.d(
//                                    UPLOAD, "Split Upload progress ${
//                                        initRep.data.node[buffers.indexOf(
//                                            buffer
//                                        )].url
//                                    } ---->>>  split trunk bytesWritten: $bytesWritten, $contentLength, split trunk progress: $progress || Total progress: $totalProgress"
//                                )
                            val emitProgress = progressList[message.clientMsgID]
                            if (emitProgress != null) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    emitProgress.value = totalProgress.toFloat()
                                }

                                if (emitProgress.value == 1.0f) {
                                    newTask.progress = 1.0f
                                    newTask.reqBodyJson =
                                        Gson().toJson(mergeMultiUploadReqQueue[message.clientMsgID])
                                    viewModelScope.launch(Dispatchers.IO) {
                                        findMediaMsgAndUpdateProgress(
                                            message.clientMsgID, uploadTask
                                        )
                                    }
                                }
                            }
                        })

                    val bIndex = buffers.indexOf(buffer)
                    Log.d(CONTINUE_UPLOAD, "分块上传 index --->>> $bIndex")
                    UploadRepository.uploadBinary(
                        url = initRep.data.node[bIndex].url, bodyFromBuffer
                    )

                    Log.d(CONTINUE_UPLOAD, "合 part executeIndex --->>> $executeIndex")
                    mergeMultiUploadReqQueue[message.clientMsgID]?.part?.add(
                        Part(
                            partNumber = executeIndex, etag = hexString
                        )
                    )
                    uploadTasks[message.clientMsgID]?.executeIndex = executeIndex
                    deltaSize += buffer.array().size
                    executeIndex++
                }
            }

            val reqBody = mergeMultiUploadReqQueue[message.clientMsgID]
            Log.d(UPLOAD, "merge reqBody --->> $reqBody")
            if (reqBody != null) {
                val mergeUpload = UploadRepository.completeMultiPartUpload(
                    body = reqBody
                )

                UploadBigFileUtils.releaseMemory(file.hashCode())
                Log.d(
                    UPLOAD, "BigFileUpload 断点续传 success ---> ${mergeUpload.data.location}"
                )
                val cacheMediaSource = Gson().fromJson(message.msgBody, MediaSource::class.java)
                val mediaSource = MediaSource(
                    width = message.localFile?.width ?: 0,
                    height = message.localFile?.height ?: 0,
                    thumbnail = Thumbnail(
                        url = cacheMediaSource.thumbnail.url, key = cacheMediaSource.thumbnail.key
                    ),
                    resource = Resource(
                        url = mergeUpload.data.location, key = ""
                    )
                )
                val sendBody = entityToMediaSendBody(message)
                sendBody.msgBody = Gson().toJson(mediaSource)
                senMessageFromMedia(sendBody, preSend = {
                    viewModelScope.launch(Dispatchers.IO) {
                        IMLocalRepository.findMediaMsgAndUpdateBody(
                            sendBody.clientMsgId, sendBody.msgBody
                        )
                    }
                })
            }

        }
        jobs[message.clientMsgID] = job
    }

    fun cancelJob(clientMsgId: String, progress: State<Float>?) {
        val job = jobs[clientMsgId]
        job?.cancel()
        progress?.let {
            viewModelScope.launch(Dispatchers.IO) {
                Log.d(UPLOAD, "暂停上传进度: ${it.value}")
                val mergeReq = mergeMultiUploadReqQueue[clientMsgId]
                val uploadTask = uploadTasks[clientMsgId]
                Log.d(UPLOAD, "暂停截取 mergeReq ---->>> $mergeReq")
                Log.d(UPLOAD, "暂停截取 uploadTask ---->>> $uploadTask")
                uploadTask?.progress = progress.value
                uploadTask?.reqBodyJson = Gson().toJson(mergeReq)
                findMediaMsgAndUpdateProgress(clientMsgId, uploadTask = uploadTask)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun uploadThumbnail(
        mediaMessage: MediaMessage, clientMsgId: String
    ): SingleUploadRep {
        val bmp = if (mediaMessage.isImage) {
            ThumbnailUtils.createImageThumbnail(
                mediaMessage.file, Size(mediaMessage.width, mediaMessage.height), null
            )
        } else {
            ThumbnailUtils.createVideoThumbnail(
                mediaMessage.file, Size(mediaMessage.width, mediaMessage.height), null
            )
        }
        uploadThumbs[clientMsgId] = MutableStateFlow(bmp)
        val bao = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao)
        val buffer = bao.toByteArray()
        val thumbnailResp = UploadRepository.singleUpload(
            file = MultipartBody.Part.createFormData(
                "file", "lbe_${uuidGen()}_${timeStampGen()}.png", buffer.toRequestBody()
            ), signType = 2
        )
        withContext(Dispatchers.IO) {
            bao.close()
        }
        Log.d(UPLOAD, "thumbnail upload ---->>> ${thumbnailResp.data.paths[0]}")
        return thumbnailResp
    }

    private fun attachMsgToUI(message: MessageEntity) {
        Log.d(TAG, "attachMsgToUI: $message")
        val messages = uiState.value?.messages?.toMutableList()
        messages?.add(message)
        _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })

//        _messages.update { state ->
//            val news = state.toMutableList()
//            news.add(message)
//            news
//        }
    }

    private fun clearInput() {
        viewModelScope.launch {
            delay(50)
            _inputMsg.postValue("")
        }
    }
}

