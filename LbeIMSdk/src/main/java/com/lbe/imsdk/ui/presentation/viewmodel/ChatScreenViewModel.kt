package com.lbe.imsdk.ui.presentation.viewmodel

import NetworkMonitor
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.lbe.imsdk.data.local.IMLocalRepository
import com.lbe.imsdk.data.local.IMLocalRepository.findMediaMsgAndUpdateProgress
import com.lbe.imsdk.data.remote.LbeConfigRepository
import com.lbe.imsdk.data.remote.LbeImRepository
import com.lbe.imsdk.data.remote.UploadRepository
import com.lbe.imsdk.model.InitArgs
import com.lbe.imsdk.model.LocalMediaFile
import com.lbe.imsdk.model.MediaMessage
import com.lbe.imsdk.model.MessageEntity
import com.lbe.imsdk.model.TempUploadInfo
import com.lbe.imsdk.model.UploadTask
import com.lbe.imsdk.model.proto.IMMsg
import com.lbe.imsdk.model.req.CompleteMultiPartUploadReq
import com.lbe.imsdk.model.req.ConfigBody
import com.lbe.imsdk.model.req.FaqReqBody
import com.lbe.imsdk.model.req.HistoryBody
import com.lbe.imsdk.model.req.InitMultiPartUploadBody
import com.lbe.imsdk.model.req.MarkReadReqBody
import com.lbe.imsdk.model.req.MsgBody
import com.lbe.imsdk.model.req.Pagination
import com.lbe.imsdk.model.req.Part
import com.lbe.imsdk.model.req.SeqCondition
import com.lbe.imsdk.model.req.SessionBody
import com.lbe.imsdk.model.req.SessionListReq
import com.lbe.imsdk.model.resp.InitMultiPartUploadRep
import com.lbe.imsdk.model.resp.MediaSource
import com.lbe.imsdk.model.resp.Resource
import com.lbe.imsdk.model.resp.SessionEntry
import com.lbe.imsdk.model.resp.SingleUploadRep
import com.lbe.imsdk.model.resp.Thumbnail
import com.lbe.imsdk.service.ChatService
import com.lbe.imsdk.service.DynamicHeaderUrlRequestFactory
import com.lbe.imsdk.service.RetrofitInstance
import com.lbe.imsdk.ui.presentation.components.ProgressRequestBody
import com.lbe.imsdk.ui.presentation.components.ProgressRequestBody.Companion.toRequestBody
import com.lbe.imsdk.ui.presentation.screen.ChatScreenUiState
import com.lbe.imsdk.utils.Converts.entityToMediaSendBody
import com.lbe.imsdk.utils.Converts.entityToSendBody
import com.lbe.imsdk.utils.Converts.protoToEntity
import com.lbe.imsdk.utils.Converts.protoTypeConvert
import com.lbe.imsdk.utils.Converts.sendBodyToEntity
import com.lbe.imsdk.utils.FileLogger
import com.lbe.imsdk.utils.TimeUtils.timeStampGen
import com.lbe.imsdk.utils.UUIDUtils.uuidGen
import com.lbe.imsdk.utils.UploadBigFileUtils
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.time.Duration
import java.util.Timer
import java.util.TimerTask

enum class ConnectionStatus {
    NOT_STARTED, OPENED, CLOSED, CONNECTING, CLOSING, FAILED, RECEIVED
}

class ChatScreenViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "LbeIM_Websocket"
        const val REALM = "RealmTAG"
        const val UPLOAD = "LbeIM_UPLOAD"
        const val FILE_SELECT = "File Select"
        const val IMAGE_ENCRYPTION = "Image Encryption"
        const val CONTINUE_UPLOAD = "CONTINUE_UPLOAD"
        const val RETROFIT = "Lbe Retrofit"
        var lbeSign = ""
        var uid = ""
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
        var userAvatar: String = ""
        var lbeIdentity: String = ""
        var progressList: MutableMap<String, MutableStateFlow<Float>> = mutableMapOf()
        val tempUploadInfos: MutableMap<String, TempUploadInfo> = mutableMapOf()
        var sdkInit: Boolean = false
        var endSession: Boolean = false
        var isAnonymous: Boolean = false
    }

    private val jobs: MutableMap<String, Job> = mutableMapOf()
    private val mergeMultiUploadReqQueue: MutableMap<String, CompleteMultiPartUploadReq> =
        mutableMapOf()
    private val uploadTasks: MutableMap<String, UploadTask> = mutableMapOf()

    private val _uiState = MutableLiveData(ChatScreenUiState())
    val uiState: LiveData<ChatScreenUiState> = _uiState
    private var allMessageSize = 0

    var recivCount = MutableStateFlow(0)
    var toBottom = MutableStateFlow("")
    var recived = MutableStateFlow("")
    var lastCsMessage: MessageEntity? = null

    var isTimeOut = MutableStateFlow(false)
    var timeOutConfigOpen = MutableStateFlow(false)
    var timeOutTimer: Timer? = null
    var timeOut: Long = 5

    var kickOfflineEvent = MutableStateFlow(value = "")
    var kickOffLine = MutableStateFlow(false)
    var faqNotExistEvent = MutableStateFlow(value = "")

    private lateinit var initArgs: InitArgs

//    private val _messages = MutableStateFlow<List<MessageEntity>>(mutableListOf())
//    val messageList = _messages

    private val _inputMsg = MutableLiveData("")
    val inputMsg: LiveData<String> = _inputMsg

    private lateinit var okHttpClient: OkHttpClient
    private var chatService: ChatService? = null
    private var wsSubs: Disposable? = null

    var lazyListState: LazyListState? = null

    private val networkMonitor = NetworkMonitor(application)

    val isConnected = networkMonitor.isConnected

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        println("coroutineExceptionHandler --->> Unhandled exception: ${throwable.message}")
    }

    private var pingTimer: Timer? = null

    private val fileLogger = FileLogger(application)

    init {
        networkMonitor.startMonitoring()
        // testOfflineTakeByCache()
    }

    override fun onCleared() {
        networkMonitor.stopMonitoring()
        disConnection()
        println("LbeChat Lifecycle --->> ChatScreenViewModel onCleared")
        super.onCleared()
    }

    private fun disConnection() {
        if (sdkInit) {
            Log.d(TAG, "websocket disConnectionSdk")
            sdkInit = false
            wsSubs?.dispose()
            okHttpClient.dispatcher.executorService.shutdown()
            jobs["sdkJob"]?.cancel()
            pingTimer?.cancel()
            pingTimer = null
        }
    }

    private fun testOfflineTakeByCache() {
        currentSession = SessionEntry(sessionId = "cn-43ro83fqqzm2", latestMsg = null)
        lbeSession = "cn-43ro83fqqzm2"
        syncPageInfo(currentSession)
        viewModelScope.launch(Dispatchers.IO) {
            filterLocalMessages()
            scrollToBottom()
        }
    }

    fun initSdk(args: InitArgs) {
        lbeSign = args.lbeSign
        nickId = args.nickId.ifEmpty {
            sharedPreferences.edit().putBoolean("needSaveNickId", true).apply()
            isAnonymous = true
            sharedPreferences.getString("anonymousNickId", "").toString()
        }
        nickName = args.nickName
        userAvatar = args.headerIcon
        lbeIdentity = args.lbeIdentity
        initArgs = args
        realInitSdk()
    }

    private fun realInitSdk() {
        val sdkJob = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                println("NetworkMonitor [prepare]---->>> ${networkMonitor.isNetworkAvailable()}")
                if (!networkAvailable()) {
                    return@launch
                }
                delay(100)
                fetchConfig()
                createSession()
                viewModelScope.launch(Dispatchers.IO) {
                    fetchSessionList()
                    observerConnection()
                    fetchTimeoutConfig()
                    faq(faqReqBody = FaqReqBody(faqType = 0, id = ""))
                    sdkInit = true
                    schedulePingJob()
                }
            } catch (e: Exception) {
                println("realInitSdk error: $e")
            }
        }
        jobs["sdkJob"] = sdkJob
    }

    private fun networkAvailable(): Boolean {
        return networkMonitor.isNetworkAvailable()
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: HttpException) {
            println("SafeApiCall HTTP error: ${e.code()} - ${e.message()}")
            Result.failure(e)
        } catch (e: IOException) {
            println("SafeApiCall Network error: ${e.localizedMessage}")
            Result.failure(e)
        } catch (e: Exception) {
            println("SafeApiCall Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun fetchConfig() {
        if (!networkAvailable()) {
            return
        }
        val result = safeApiCall {
            LbeConfigRepository.fetchConfig(lbeSign, lbeIdentity, ConfigBody(0, 1))
        }
        result.onSuccess { config ->
            Log.d(RETROFIT, "获取配置: $config")
            wssHost = config.data.ws[0]
            RetrofitInstance.IM_URL = config.data.rest[0]
            RetrofitInstance.UPLOAD_BASE_URL = config.data.oss[0]
        }.onFailure { err ->
            Log.d(RETROFIT, "获取配置异常: $err")
        }
    }

    private suspend fun createSession() {
        if (!networkAvailable()) {
            return
        }
        val result = safeApiCall {
            LbeImRepository.createSession(
                lbeSign, lbeIdentity, SessionBody(
                    identityID = initArgs.lbeIdentity,
                    nickId = nickId,
                    nickName = nickName,
                    phone = initArgs.phone,
                    email = initArgs.email,
                    language = initArgs.language,
                    device = initArgs.device,
                    source = initArgs.source,
                    headIcon = initArgs.headerIcon,
                    groupID = initArgs.groupID,
                    uid = "",
                    extraInfo = "",
                )
            )
        }
        result.onSuccess { session ->
            Log.d(RETROFIT, "创建会话: $session")
            lbeToken = session.data.token
            lbeSession = session.data.sessionId
            uid = session.data.uid
            if (sharedPreferences.getBoolean("needSaveNickId", false)) {
                nickId = session.data.nickId
                sharedPreferences.edit().putString("anonymousNickId", session.data.nickId).apply()
                sharedPreferences.edit().putBoolean("needSaveNickId", false).apply()
            }
            endSession = false
        }.onFailure { error ->
            Log.d(RETROFIT, "创建会话异常: $error")
        }
    }

    private suspend fun fetchSessionList() {
        if (!networkAvailable()) {
            return
        }
        val result = safeApiCall {
            LbeImRepository.fetchSessionList(
                lbeToken = lbeToken, lbeIdentity = lbeIdentity, body = SessionListReq(
                    pagination = Pagination(
                        pageNumber = 1, showNumber = 1000
                    ), sessionType = 2
                )
            )
        }
        result.onSuccess { sessionListRep ->
            Log.d(RETROFIT, "会话列表: $sessionListRep")
            sessionList.addAll(sessionListRep.data.sessionList)
            currentSession = sessionList[currentSessionIndex]
            seq = currentSession?.latestMsg?.msgSeq ?: 0
            remoteLastMsgType = currentSession?.latestMsg?.msgType ?: 0
            checkNeedSyncRemote()
            syncPageInfo(currentSession)
            filterLocalMessages()
            scrollToBottom()
            syncPendingJobs()
        }.onFailure { err ->
            Log.d(RETROFIT, "会话列表异常: $err")
        }
    }

    fun loadHistory() {
        if (currentSessionIndex >= sessionList.size - 1) {
            return
        }
        currentSessionIndex += 1
        currentSession = sessionList[currentSessionIndex]
        viewModelScope.launch(Dispatchers.IO) {
            checkNeedSyncRemote()
            syncPageInfo(currentSession)
            filterLocalMessages()
        }
    }

    private fun syncPageInfo(currentSession: SessionEntry?) {
        val cacheMessages = IMLocalRepository.filterMessages(currentSession?.sessionId ?: "")
        Log.d(
            REALM,
            "syncPageInfo cacheMessages size---->>> ${cacheMessages.size}, currentSession: ${currentSession?.sessionId}"
        )
        if (cacheMessages.isNotEmpty()) {
            currentSessionTotalPages = Math.max(cacheMessages.size / showPageSize, 1)
            currentPage = currentSessionTotalPages
            Log.d(
                REALM,
                "syncPageInfo ---->>> currentSessionTotalPages: $currentSessionTotalPages, currentPage: $currentPage"
            )
        } else {
            currentPage = 1
            currentSessionTotalPages = 1
        }
    }

    suspend fun checkNeedSyncRemote() {
        val cacheMessages = IMLocalRepository.filterMessages(currentSession?.sessionId ?: "")
        Log.d(
            REALM,
            "checkNeedSyncRemote --->>> cache size: ${cacheMessages.size} |  remote lastSeq: $seq , remoteLastMsgType: $remoteLastMsgType"
        )
        if (cacheMessages.size < seq) {
            fetchHistoryAndSync(currentSession)
        }
    }

    private fun afterSendUpdateList() {
        currentSessionIndex = 0
        currentSession = sessionList[currentSessionIndex]
        val cacheMessages = IMLocalRepository.filterMessages(currentSession?.sessionId ?: "")
        currentSessionTotalPages = cacheMessages.size / showPageSize
        currentPage = currentSessionTotalPages
        val subList = pagination(cacheMessages)
        viewModelScope.launch(Dispatchers.Main) {
            val messages = uiState.value?.messages?.toMutableList()
            messages?.clear()
            messages?.addAll(subList)
            allMessageSize = messages?.size ?: 0
            _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
            Log.d(
                REALM,
                "发送完更新列表 ---->>> messages: ${messages?.size}, allMessageSize: $allMessageSize, \n $messages"
            )
        }
    }

    fun filterLocalMessages(
        sid: String = currentSession?.sessionId ?: "",
    ) {
        Log.d(
            REALM,
            "分页 ---->>> currentSessionIndex: $currentSessionIndex ,sessionId: $sid, currentSessionTotalPages: $currentSessionTotalPages, currentPage: $currentPage, seq: $seq"
        )
        if ((currentSessionTotalPages != 0 && currentPage > currentSessionTotalPages) || currentPage < 1) return

        val cacheMessages = IMLocalRepository.filterMessages(sid)
        val subList = pagination(cacheMessages)

        viewModelScope.launch(Dispatchers.Main) {
            val messages = uiState.value?.messages?.toMutableList()
            messages?.addAll(0, subList)
            allMessageSize = messages?.size ?: 0
            _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
            Log.d(
                REALM,
                "分页后 messageList size --->> ${messages?.size}, allMessageSize: $allMessageSize"
            )
        }
    }

    private fun pagination(source: List<MessageEntity>): List<MessageEntity> {
        currentSessionTotalPages = source.size / showPageSize
        val yu = source.size % showPageSize
        Log.d(REALM, "分页总页数: $currentSessionTotalPages, currentPage: $currentPage, 取余: $yu")

        val subList = if (currentPage == 1 && yu != 0) {
            val start = Math.max((currentPage - 1) * showPageSize, 0)
            val end = Math.min(currentPage * showPageSize + yu, source.size)
            Log.d(REALM, "最后一页 --->>> currentPage: $currentPage, start: $start, end: $end")
            source.subList(start, end)
        } else {
            val start = Math.max(
                source.size - showPageSize * (currentSessionTotalPages - (currentPage - 1)), 0
            )
            val end = Math.min(start + showPageSize, source.size)
            Log.d(
                REALM,
                "非最后一页 --->>> source.size: ${source.size}, currentPage: $currentPage, start: $start, end: $end"
            )
            source.subList(start, end)
        }
        return subList
    }

    fun scrollToBottom() {
        toBottom.value += ","
    }

    fun resetRecivCount() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(99)
            recivCount.update { 0 }
        }
    }

    private suspend fun fetchTimeoutConfig() {
        if (!networkAvailable()) {
            return
        }
        val result = safeApiCall {
            LbeImRepository.fetchTimeoutConfig(
                lbeSign = lbeSign,
                lbeToken = lbeToken,
                lbeIdentity = lbeIdentity,
            )
        }
        result.onSuccess { timeoutConfig ->
            timeOut = timeoutConfig.data.timeout
            timeOutConfigOpen.update { timeoutConfig.data.isOpen }
            Log.d(REALM, "FetchTimeoutConfig ---->>> $timeoutConfig, timeOut: $timeOut")
        }.onFailure { err ->
            println("网络异常 --->>> FetchTimeoutConfig error --->>>  $err")
        }
    }

    fun markRead(message: MessageEntity) {
        if (!networkAvailable()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                val markRead = LbeImRepository.markRead(
                    lbeSign = lbeSign,
                    lbeToken = lbeToken,
                    lbeIdentity = lbeIdentity,
                    body = MarkReadReqBody(sessionId = message.sessionId, seq = message.msgSeq)
                )
                Log.d(REALM, "MarkRead ---->>> $markRead")
                IMLocalRepository.findMsgAndMarkMeRead(message.clientMsgID)
            } catch (e: Exception) {
                println("Mark Msg Read error --->>>  $e")
            }
        }
    }

    fun faq(faqReqBody: FaqReqBody) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!networkAvailable()) {
                return@launch
            }
            delay(500)
            val result = safeApiCall {
                LbeImRepository.faq(
                    lbeSession = lbeSession,
                    lbeToken = lbeToken,
                    lbeIdentity = lbeIdentity,
                    body = faqReqBody
                )
            }
            result.onSuccess { faqResp ->
                Log.d(RETROFIT, "什么 --->> $faqResp")
                if (faqResp.code == 20006) {
                    faqNotExistEvent.value += ","
                }
            }
            result.onFailure { err ->
                println("网络异常 --->>> Fetch faq  error --->>>  $err")
            }
        }
    }

    fun turnCustomerService() {
        if (!sdkInit) {
            return
        }
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                val turnCSResp = LbeImRepository.turnCustomerService(
                    lbeSign = lbeSign,
                    lbeToken = lbeToken,
                    lbeIdentity = lbeIdentity,
                    lbeSession = lbeSession,
                )
            } catch (e: Exception) {
                Log.d(RETROFIT, "Fetch turnCSResp  error: $e")
            }
        }
    }

    private suspend fun fetchHistoryAndSync(currentSession: SessionEntry?) {
        if (!networkAvailable()) {
            return
        }

        val result = safeApiCall {
            LbeImRepository.fetchHistory(
                lbeSign = lbeSign,
                lbeToken = lbeToken,
                lbeIdentity = lbeIdentity,
                body = HistoryBody(
                    sessionId = currentSession?.sessionId ?: "", seqCondition = SeqCondition(
                        startSeq = 0, endSeq = currentSession?.latestMsg?.msgSeq ?: 0
                    )
                )
            )
        }

        result.onSuccess { history ->
            Log.d(RETROFIT, "会话历史: ${history.data.content.size}")
            Log.d(REALM, "History sync")
            if (history.data.content.isNotEmpty()) {
                seq = history.data.content.last().msgSeq
                for (content in history.data.content) {
                    when (content.msgType) {
                        1, 2, 4, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13 -> {
                            val entity = MessageEntity().apply {
                                sessionId = content.sessionId
                                senderUid = content.senderUid
                                msgBody = content.msgBody
                                clientMsgID = content.clientMsgID
                                msgType = content.msgType
                                sendTime = content.sendTime.toLong()
                                msgSeq = content.msgSeq
                                readed = (content.status == 1)
                                customerServiceNickname = content.senderNickname
                                customerServiceAvatar = content.senderFaceURL
                            }
                            IMLocalRepository.insertMessage(entity)
                        }

                        else -> {
                            continue
                        }
                    }
                }
            }
            syncPageInfo(currentSession)
        }.onFailure { err ->
            Log.d(RETROFIT, "会话历史异常: $err")
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

    @SuppressLint("CheckResult")
    private fun observerConnection() {
        okHttpClient =
            OkHttpClient.Builder().connectTimeout(Duration.ofDays(5)).retryOnConnectionFailure(true)
                .build()

        val scarletInstance = Scarlet.Builder().webSocketFactory(
            okHttpClient.newWebSocketFactory(
                DynamicHeaderUrlRequestFactory(
                    url = wssHost, lbeToken = lbeToken, lbeSession = lbeSession,
                )
            )
        ).addStreamAdapterFactory(RxJava2StreamAdapterFactory()).build()

        chatService = scarletInstance.create<ChatService>()

        Log.d(TAG, "Observing Connection")
        updateConnectionStatus(ConnectionStatus.CONNECTING)

        wsSubs = chatService?.observeConnection()?.subscribe(
            { response ->
                onResponseReceived(response)
            },
            { error ->
                error.localizedMessage?.let { Log.e(TAG, "websocket 出错 ---->>> $it") }
            },
        )
    }

    private fun onResponseReceived(response: WebSocket.Event) {
        Log.d(TAG, "webSocket response --->>> $response")

        when (response) {
            is OnConnectionOpened<*> -> updateConnectionStatus(ConnectionStatus.OPENED)

            is OnConnectionClosed -> updateConnectionStatus(ConnectionStatus.CLOSED)

            is OnConnectionClosing -> updateConnectionStatus(ConnectionStatus.CLOSING)

            is OnConnectionFailed -> updateConnectionStatus(ConnectionStatus.FAILED)

            is OnMessageReceived -> handleOnMessageReceived(response.message)
        }
    }

    private fun handleOnMessageReceived(message: Message) {
        try {
            val value = (message as Message.Bytes).value
            viewModelScope.launch {
                val msgEntity = IMMsg.MsgEntityToFrontEnd.parseFrom(value)
                Log.d(TAG, "handleOnMessageReceived protobuf bytes --->>>  $msgEntity")
                fileLogger.log(TAG, "handleOnMessageReceived protobuf bytes --->>>  $msgEntity")

                if (msgEntity.msgBody.senderUid == "111") {
                    return@launch
                }

                when (msgEntity.msgType) {
                    IMMsg.MsgType.TextMsgType -> {
                        remoteLastMsgType = protoTypeConvert(msgEntity)

                        val receivedSeq = msgEntity.msgBody.msgSeq
                        println("接收转人工系统消息 --->> remoteLastMsgType: $remoteLastMsgType ,receivedSeq: $receivedSeq, seq: $seq")
                        if (remoteLastMsgType == 1 || remoteLastMsgType == 2 || remoteLastMsgType == 3 || remoteLastMsgType == 8 || remoteLastMsgType == 9 || remoteLastMsgType == 10 || remoteLastMsgType == 12) {
                            if (receivedSeq - seq > 2) {
                                fetchHistoryAndSync(sessionList[0])
                            }
                            seq = receivedSeq
                            recivCount.value += 1
                        }
                        Log.d(
                            TAG, "收到消息 --->> seq: $seq, remoteLastMsgType: $remoteLastMsgType"
                        )
                        val entity = protoToEntity(msgEntity)
                        println("接收转人工系统消息 --->>> $entity")

                        lastCsMessage = entity
                        if (timeOutConfigOpen.value) {
                            scheduleTimeoutJob()
                        }
                        viewModelScope.launch {
                            IMLocalRepository.insertMessage(entity)
                            if (entity.senderUid != uid) {
                                addSingleMsgToUI(entity)
                                recived.value += ","
                            }
                        }
                    }

                    IMMsg.MsgType.HasReadReceiptMsgType -> {
                        val hasReadMsg = msgEntity.hasReadReceiptMsg
                        val sessionId = hasReadMsg.sessionID
                        val markReadList = hasReadMsg.hasReadSeqsList

                        Log.d(
                            TAG,
                            "收到客服标记已读消息 --->> sessionId: $sessionId, markReadList: $markReadList}"
                        )
                        for (seq in markReadList) {
                            IMLocalRepository.findMsgAndMarkCsRead(sessionId, seq.toInt())
                        }
                        markMsgReadFromUI(sessionId, markReadList)
                    }

                    IMMsg.MsgType.EndSessionMsgType -> {
                        endSession = true
                    }

                    IMMsg.MsgType.KickOffLineMsgType -> {
                        disConnection()
                        kickOfflineEvent.value += ","
                        kickOffLine.update { true }
                    }

                    else -> {
                        Log.d(TAG, "Not Impl yet ---> ${msgEntity.msgType}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleOnMessageReceived: ", e)
        }
    }

    private fun schedulePingJob() {
        val period = 1000 * 15L
        val toServer = IMMsg.MsgEntityToServer.newBuilder().setMsgType(IMMsg.MsgType.TextMsgType)
            .setMsgBody(IMMsg.MsgBody.newBuilder().setMsgBody("ping").build()).build()
        if (pingTimer == null) pingTimer = Timer()
        pingTimer?.schedule(object : TimerTask() {
            override fun run() {
                val sendStatus = chatService?.sendMessage(toServer.toByteArray())
                Log.d("Ping Job", "$toServer ---->>> $sendStatus")
                fileLogger.log("Ping Job", "$toServer ---->>> $sendStatus")
            }
        }, period, period)
    }

    private fun scheduleTimeoutJob() {
        val period = 1000 * 60 * timeOut
        if (timeOutTimer == null) {
            Log.d("TimeOut", "超时提醒，period: $period")
            timeOutTimer = Timer()
            timeOutTimer?.schedule(object : TimerTask() {
                override fun run() {
                    Log.d("TimeOut", "超时提醒， seq: $seq, last: ${lastCsMessage?.msgSeq}")
                    if (lastCsMessage?.msgSeq!! <= seq) {
                        Log.d("TimeOut", "超时提醒，用户没回复")
                        isTimeOut.update { _ -> true }
                        timeOutTimer?.cancel()
                        timeOutTimer = null
                    }
                }
            }, period)
        } else {
            timeOutTimer?.cancel()
            timeOutTimer = null
            timeOutTimer = Timer()
            timeOutTimer?.schedule(object : TimerTask() {
                override fun run() {
                    if (lastCsMessage?.msgSeq!! <= seq) {
                        Log.d(
                            "TimeOut",
                            "客服回复重启，用户没回复， seq: $seq, last: ${lastCsMessage?.msgSeq}"
                        )
                        isTimeOut.update { _ -> true }
                        timeOutTimer?.cancel()
                        timeOutTimer = null
                    }
                }
            }, period)
        }
    }

    private fun updateConnectionStatus(connectionStatus: ConnectionStatus) {
        Log.d(TAG, "websocket update status --->>> ${connectionStatus.name}")
        fileLogger.log(TAG, "websocket update status --->>> ${connectionStatus.name}")
//        viewModelScope.launch(Dispatchers.Main) {
//            _uiState.postValue(_uiState.value?.copy(connectionStatus = connectionStatus))
//        }
    }

    fun onMessageChange(message: String) {
        _inputMsg.postValue(message)
    }

    fun sendMessageFromTextInput(messageSent: () -> Unit, trimToast: () -> Unit) {
        if ((_inputMsg.value?.trim() ?: "").isEmpty()) {
            _inputMsg.postValue("")
            trimToast()
            messageSent()
            return
        }

        if (endSession) {
            sessionList.clear()
            currentSessionIndex = 0
            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                createSession()
                fetchSessionList()
                faq(faqReqBody = FaqReqBody(faqType = 0, id = ""))
                val sendBody = genMsgBody(type = 1, msgBody = _inputMsg.value ?: "")
                send(
                    messageSent = messageSent,
                    preSend = {
                        insertCacheMaybeUpdateUI(sendBody, localFile = null, updateUI = false)
                    },
                    sendBody,
                )
            }
            return
        }

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
        if (endSession) {
            sessionList.clear()
            currentSessionIndex = 0
            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                createSession()
                fetchSessionList()
                faq(faqReqBody = FaqReqBody(faqType = 0, id = ""))
                send(messageSent = {}, preSend = preSend, msgBody = msgBody)
            }
            return
        }

        send(messageSent = {}, preSend = preSend, msgBody = msgBody)
    }

    private fun send(messageSent: () -> Unit, preSend: () -> Unit, msgBody: MsgBody) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            preSend()
            try {
                val senMsg = LbeImRepository.sendMsg(
                    lbeToken = lbeToken,
                    lbeIdentity = lbeIdentity,
                    lbeSession = lbeSession,
                    body = msgBody
                )
                seq = senMsg.data.msgReq
                if (lastCsMessage != null) {
                    Log.d("TimeOut", "seq: $seq, lastCsSeq: ${lastCsMessage!!.msgSeq}")
                    if (seq > (lastCsMessage?.msgSeq ?: 0)) {
                        isTimeOut.update { false }
                        timeOutTimer?.cancel()
                        timeOutTimer = null
                    }
                }
                remoteLastMsgType = msgBody.msgType
                IMLocalRepository.findMsgAndSetSeq(msgBody.clientMsgId, seq)
            } catch (e: Exception) {
                println("send error -->> $e")
                IMLocalRepository.findMsgAndSetStatus(msgBody.clientMsgId, false)
            } finally {
                afterSendUpdateList()
                scrollToBottom()
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
        if (!networkAvailable()) {
            return
        }

        val entity = IMLocalRepository.findMsgByClientMsgId(clientMsgId)
        var newClientMsgId = ""
        if (entity != null) {
            val list = entity.clientMsgID.split("-").toMutableList()
            list.removeLast()
            list.add(timeStampGen().toString())
            newClientMsgId = list.joinToString(separator = "-")
            val body = entityToSendBody(entity, newClientMsgId)
            println("reSend ====>>> old: ${entity.clientMsgID}, new: $newClientMsgId")
            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
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
                    println("reSend error -->> $e")
                } finally {
                    afterSendUpdateList()
                    scrollToBottom()
                }
            }
        }
    }

    fun preInsertUpload(mediaMessage: MediaMessage) {
        val sendBody = genMsgBody(
            type = if (mediaMessage.isImage) 2 else 3,
        )

        val localFile = genLocalFile(mediaMessage)
        localFile.isBigFile = mediaMessage.fileSize > UploadBigFileUtils.defaultChunkSize

        val entity = insertCacheMaybeUpdateUI(
            sendBody = sendBody, localFile = localFile
        )
        tempUploadInfos[entity.clientMsgID] =
            TempUploadInfo(sendBody = sendBody, mediaMessage = mediaMessage)
    }

    fun upload(
        message: MessageEntity, thumbBitmap: Bitmap, context: Context
    ) {
        Log.d(UPLOAD, "upload file size---->>> ${message.localFile?.size}")
        if (!networkAvailable()) {
            return
        }
        message.localFile?.size?.let {
            val inputStream =
                context.contentResolver.openInputStream((message.localFile?.path ?: "").toUri())
            inputStream?.let { stream ->
                try {
                    if (it > UploadBigFileUtils.defaultChunkSize) {
                        bigFileUpload(message, thumbBitmap, stream)
                    } else {
                        singleUpload(message, thumbBitmap, stream)
                    }
                } catch (e: Exception) {
                    println("upload error ---> $e")
                }
            }
        }
    }

    private fun singleUpload(
        message: MessageEntity, thumbBitmap: Bitmap, inputStream: InputStream
    ) {
        val tempUploadInfo = tempUploadInfos[message.clientMsgID]
        val thumbWidth = thumbBitmap.width
        val thumbHeight = thumbBitmap.height
        tempUploadInfo?.let {
            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                try {
                    val thumbnailResp = uploadThumbnail(thumbBitmap)

                    val rep = UploadRepository.singleUpload(
                        file = MultipartBody.Part.createFormData(
                            "file",
                            it.mediaMessage.fileName,
                            ProgressRequestBody(delegate = inputStream.readBytes().toRequestBody(),
                                listener = { bytesWritten, contentLength ->
                                    val progress = (1.0 * bytesWritten) / contentLength
                                    Log.d(
                                        UPLOAD,
                                        "Single upload  ${it.mediaMessage.fileName} ---->>>  bytesWritten: $bytesWritten, $contentLength, progress: $progress"
                                    )
                                    val emitProgress = progressList[message.clientMsgID]
                                    if (emitProgress != null) {
                                        viewModelScope.launch(Dispatchers.Main) {
                                            emitProgress.value = progress.toFloat()
                                        }

                                        if (emitProgress.value == 1.0f) {
                                            val uploadTask = UploadTask()
                                            uploadTask.progress = 1.0f
                                            viewModelScope.launch(Dispatchers.IO) {
                                                findMediaMsgAndUpdateProgress(
                                                    message.clientMsgID, uploadTask
                                                )
                                            }
                                        }
                                    }
                                })
                        ), signType = if (it.mediaMessage.isImage) 2 else 1, token = lbeToken
                    )
                    Log.d(UPLOAD, "Single upload ---->>> ${rep.data.paths[0]}")
                    val mediaSource = MediaSource(
                        width = thumbWidth, height = thumbHeight, thumbnail = Thumbnail(
                            url = thumbnailResp.data.paths[0].url,
                            key = thumbnailResp.data.paths[0].key
                        ), resource = Resource(
                            url = rep.data.paths[0].url, key = rep.data.paths[0].key
                        )
                    )
                    it.sendBody.msgBody = Gson().toJson(mediaSource)
                    senMessageFromMedia(it.sendBody, preSend = {
                        viewModelScope.launch(Dispatchers.IO) {
                            IMLocalRepository.findMediaMsgAndUpdateBody(
                                message.clientMsgID, it.sendBody.msgBody
                            )
                        }
                    })
                    inputStream.close()
                } catch (e: Exception) {
                    Log.d(UPLOAD, "Single upload error --->> $e")
                }
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
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            IMLocalRepository.insertMessage(entity)
            if (sessionList.isNotEmpty()) {
                syncPageInfo(sessionList[0])
            }
            if (updateUI) {
                afterSendUpdateList()
                scrollToBottom()
                progressList[entity.clientMsgID] = MutableStateFlow(0.0f)
            }
        }
        return entity
    }

    private fun genLocalFile(mediaMessage: MediaMessage): LocalMediaFile {
        val localFile = LocalMediaFile()
        localFile.fileName = mediaMessage.fileName
        localFile.path = mediaMessage.path
        localFile.size = mediaMessage.fileSize
        localFile.mimeType = mediaMessage.mime
        localFile.width = mediaMessage.height
        localFile.height = mediaMessage.width
        return localFile
    }

    private fun bigFileUpload(
        message: MessageEntity, thumbBitmap: Bitmap, inputStream: InputStream
    ) {
        try {
            val tempUploadInfo = tempUploadInfos[message.clientMsgID]
            val thumbWidth = thumbBitmap.width
            val thumbHeight = thumbBitmap.height
            tempUploadInfo?.let { it ->
                it.thumbWidth = thumbWidth
                it.thumbHeight = thumbHeight
                var executeIndex = 0
                val uploadTask = UploadTask()
                uploadTask.executeIndex = executeIndex
                uploadTasks[message.clientMsgID] = uploadTask

                mergeMultiUploadReqQueue[message.clientMsgID] = CompleteMultiPartUploadReq(
                    uploadId = "", name = it.mediaMessage.fileName, part = mutableListOf()
                )

                val job = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                    val thumbnailResp = uploadThumbnail(thumbBitmap)
                    val thumbnailSource = MediaSource(
                        width = thumbWidth, height = thumbHeight, thumbnail = Thumbnail(
                            url = thumbnailResp.data.paths[0].url,
                            key = thumbnailResp.data.paths[0].key
                        ), resource = Resource(
                            url = "", key = ""
                        )
                    )
                    it.sendBody.msgBody = Gson().toJson(thumbnailSource)
                    IMLocalRepository.findMediaMsgAndUpdateBody(
                        message.clientMsgID, it.sendBody.msgBody
                    )
                    updateSingleMessage(source = message) { m ->
                        m.msgBody = it.sendBody.msgBody
                    }
                    scrollToBottom()
                    val initRep = UploadRepository.initMultiPartUpload(
                        body = InitMultiPartUploadBody(
                            size = it.mediaMessage.fileSize,
                            name = it.mediaMessage.fileName,
                            contentType = ""
                        ),
                        token = lbeToken
                    )
                    Log.d(UPLOAD, "init multi upload --->>> $initRep")

                    IMLocalRepository.findMediaMsgUpdateCanPending(message.clientMsgID)
                    updateSingleMessage(source = message) { m ->
                        m.canPending = true
                    }

                    uploadTask.initTrunksRepJson = Gson().toJson(initRep)
                    val start = System.currentTimeMillis()
                    Log.d(
                        UPLOAD,
                        "Big file upload ---->>> fileName: ${it.mediaMessage.fileName}, Fs hash: ${it.mediaMessage.path}, split start: $start"
                    )
                    if (initRep.data.node.size > 1) {
                        UploadBigFileUtils.splitFile(
                            inputStream, UploadBigFileUtils.defaultChunkSize, it.mediaMessage.path
                        )
                    } else {
                        UploadBigFileUtils.splitFile(
                            inputStream, initRep.data.node[0].size, it.mediaMessage.path
                        )
                    }
                    val end = System.currentTimeMillis()
                    Log.d(UPLOAD, "split end: $end, diff: ${end - start}")

                    val taskLength = initRep.data.node.size
                    uploadTasks[message.clientMsgID]?.taskLength = taskLength

                    mergeMultiUploadReqQueue[message.clientMsgID] = CompleteMultiPartUploadReq(
                        uploadId = initRep.data.uploadId,
                        name = it.mediaMessage.fileName,
                        part = mutableListOf()
                    )
                    val buffers = UploadBigFileUtils.blocks[it.mediaMessage.path]

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
                                        (1.0 * (deltaSize + bytesWritten)) / it.mediaMessage.fileSize
                                    val currentBlockProgress = (1.0 * bytesWritten) / contentLength

                                    val emitProgress = progressList[message.clientMsgID]
                                    if (emitProgress != null) {
                                        viewModelScope.launch(Dispatchers.Main) {
                                            emitProgress.value = totalProgress.toFloat()
                                        }

                                        if (emitProgress.value == 1.0f) {
                                            uploadTask.progress = 1.0f
                                            uploadTask.reqBodyJson =
                                                Gson().toJson(mergeMultiUploadReqQueue[message.clientMsgID])
                                            viewModelScope.launch(Dispatchers.IO) {
                                                findMediaMsgAndUpdateProgress(
                                                    message.clientMsgID, uploadTask
                                                )
                                            }
                                        }
                                    }
                                })
                            UploadRepository.uploadBinary(
                                url = initRep.data.node[buffers.indexOf(buffer)].url, bodyFromBuffer
                            )

                            mergeMultiUploadReqQueue[message.clientMsgID]?.part?.add(
                                Part(
                                    partNumber = executeIndex + 1, etag = hexString
                                )
                            )
                            uploadTasks[message.clientMsgID]?.executeIndex = executeIndex
                            deltaSize += buffer.array().size
                            executeIndex++
                        }
                    }
                    Log.d(UPLOAD, "iter --->> ${mergeMultiUploadReqQueue[message.clientMsgID]}")
                    val mergeUpload =
                        mergeMultiUploadReqQueue[message.clientMsgID]?.let { reqBody ->
                            UploadRepository.completeMultiPartUpload(
                                body = reqBody,
                                token = lbeToken
                            )
                        }
                    UploadBigFileUtils.releaseMemory(it.mediaMessage.path)
                    Log.d(UPLOAD, "BigFileUpload success ---> ${mergeUpload?.data?.location}")
                    val mediaSource = MediaSource(
                        width = thumbWidth, height = thumbHeight, thumbnail = Thumbnail(
                            url = thumbnailResp.data.paths[0].url,
                            key = thumbnailResp.data.paths[0].key
                        ), resource = Resource(
                            url = mergeUpload?.data?.location ?: "", key = ""
                        )
                    )
                    it.sendBody.msgBody = Gson().toJson(mediaSource)
                    senMessageFromMedia(it.sendBody, preSend = {
                        viewModelScope.launch(Dispatchers.IO) {
                            IMLocalRepository.findMediaMsgAndUpdateBody(
                                message.clientMsgID, it.sendBody.msgBody
                            )
                        }
                    })
                    inputStream.close()
                }
                jobs[message.clientMsgID] = job
            }
        } catch (e: Exception) {
            Log.d(UPLOAD, "Big file upload error --->>> $e")
        }
    }

    fun continueSplitTrunksUpload(message: MessageEntity, inputStream: InputStream) {
        if (!networkAvailable()) {
            return
        }
        val job = viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            IMLocalRepository.findMediaMsgSetUploadContinue(message.clientMsgID)
            updateSingleMessage(source = message) { m ->
                m.pendingUpload = false
                m.uploadTask = message.uploadTask
            }

            val uploadTask = message.uploadTask
            val newTask = UploadTask()
            if (uploadTask != null) {
                newTask.executeIndex = uploadTask.executeIndex
                newTask.taskLength = uploadTask.taskLength
                newTask.progress = uploadTask.progress
                newTask.reqBodyJson = uploadTask.reqBodyJson
                newTask.initTrunksRepJson = uploadTask.initTrunksRepJson
                newTask.lastTrunkUploadLength = uploadTask.lastTrunkUploadLength
            }

            uploadTasks[message.clientMsgID] = newTask
            mergeMultiUploadReqQueue[message.clientMsgID] =
                Gson().fromJson(newTask.reqBodyJson, CompleteMultiPartUploadReq::class.java)

            var executeIndex = mergeMultiUploadReqQueue[message.clientMsgID]?.part?.size ?: 0

            val initRep = Gson().fromJson(
                newTask.initTrunksRepJson, InitMultiPartUploadRep::class.java
            )

            if (newTask.taskLength > 1) {
                UploadBigFileUtils.splitFile(
                    inputStream, UploadBigFileUtils.defaultChunkSize, message.localFile?.path
                )
            } else {
                UploadBigFileUtils.splitFile(
                    inputStream, initRep.data.node[0].size, message.localFile?.path
                )
            }

            val buffers = UploadBigFileUtils.blocks[message.localFile?.path]

            if (buffers != null) {
                var deltaSize = 0L
                var tempIndex = executeIndex
                for (buffer in buffers) {
                    if (tempIndex != 0) {
                        Log.d(CONTINUE_UPLOAD, "jump tempIndex: $tempIndex")
                        deltaSize += buffer.array().size
                        tempIndex--
                        continue
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
                            val totalProgress =
                                ((1.0 * (deltaSize + bytesWritten)) / message.localFile?.size!!)
                            val currentTrunkProgress = (1.0 * bytesWritten) / contentLength

                            val emitProgress = progressList[message.clientMsgID]
                            if (emitProgress != null) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    if (totalProgress.toFloat() >= emitProgress.value) {
                                        emitProgress.value = totalProgress.toFloat()
                                    }
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
                            partNumber = executeIndex + 1, etag = hexString
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
                    body = reqBody,
                    token = lbeToken
                )

                UploadBigFileUtils.releaseMemory(message.localFile?.path)
                Log.d(
                    UPLOAD, "BigFileUpload 断点续传 merge success ---> $mergeUpload"
                )
                val cacheMediaSource = Gson().fromJson(message.msgBody, MediaSource::class.java)
                val mediaSource = MediaSource(
                    width = tempUploadInfos[message.clientMsgID]?.thumbWidth ?: 100,
                    height = tempUploadInfos[message.clientMsgID]?.thumbHeight ?: 100,
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
            inputStream.close()
        }
        jobs[message.clientMsgID] = job
    }

    fun pendingUpload(clientMsgId: String, progress: State<Float>?) {
        val job = jobs[clientMsgId]
        job?.cancel()
        progress?.let {
            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                Log.d(CONTINUE_UPLOAD, "暂停上传进度: ${it.value}")
                val mergeReq = mergeMultiUploadReqQueue[clientMsgId]
                val uploadTask = uploadTasks[clientMsgId]
                Log.d(CONTINUE_UPLOAD, "暂停截取 mergeReq ---->>> $mergeReq")
                Log.d(CONTINUE_UPLOAD, "暂停截取 uploadTask ---->>> $uploadTask")
                uploadTask?.progress = progress.value
                uploadTask?.reqBodyJson = Gson().toJson(mergeReq)
                findMediaMsgAndUpdateProgress(clientMsgId, uploadTask = uploadTask)
                val msg = IMLocalRepository.findMsgByClientMsgId(clientMsgId)
                updateSingleMessage(source = msg) { m ->
                    m.uploadTask = uploadTask
                    m.pendingUpload = true
                }
            }
        }
    }

    private fun updateSingleMessage(
        source: MessageEntity?, callback: (msg: MessageEntity) -> Unit
    ) {
        val messages = uiState.value?.messages?.toMutableList()
        val cacheMsg = messages?.find { it.clientMsgID == source?.clientMsgID }
        if (cacheMsg != null) {
            val index = messages.indexOf(cacheMsg)
            val newMsg = MessageEntity.copy(cacheMsg)
            callback(newMsg)
            messages.removeAt(index)
            messages.add(index, newMsg)
            Log.d(
                REALM,
                "updateSingleMessage 查找到 msg, 并更新 --->>> old: $cacheMsg, \n new: $newMsg"
            )
        }
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
        }
    }

    private suspend fun uploadThumbnail(
        bitmap: Bitmap
    ): SingleUploadRep {
        val bao = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bao)
        val buffer = bao.toByteArray()
        val thumbnailResp = UploadRepository.singleUpload(
            file = MultipartBody.Part.createFormData(
                "file", "lbe_${uuidGen()}_${timeStampGen()}.jpg", buffer.toRequestBody()
            ), signType = 2, token = lbeToken
        )
        withContext(Dispatchers.IO) {
            bao.close()
        }
        Log.d(UPLOAD, "thumbnail upload ---->>> ${thumbnailResp.data.paths[0]}")
        return thumbnailResp
    }

    private fun addSingleMsgToUI(message: MessageEntity) {
        Log.d(TAG, "addSingleMsgToUI: $message")
        val messages = uiState.value?.messages?.toMutableList()
        messages?.add(message)
        allMessageSize = messages?.size ?: 0
        _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
    }

    private fun markMsgReadFromUI(sessionId: String, seqs: MutableList<Long>) {
        val messages = uiState.value?.messages?.toMutableList()
        for (seq in seqs) {
            val cacheMsg = messages?.find { it.sessionId == sessionId && it.msgSeq == seq.toInt() }
            if (cacheMsg != null) {
                val index = messages.indexOf(cacheMsg)
                val newMsg = MessageEntity.copy(cacheMsg)
                newMsg.readed = true
                messages.removeAt(index)
                messages.add(index, newMsg)
                Log.d(TAG, "查找到 msg --->>> $cacheMsg")
                Log.d(
                    TAG,
                    "msg after mark read--->>> ${messages.find { it.sessionId == sessionId && it.msgSeq == seq.toInt() }?.readed}"
                )
            }
        }
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.postValue(messages?.let { _uiState.value?.copy(messages = it) })
        }
    }

    private fun clearInput() {
        viewModelScope.launch {
            delay(50)
            _inputMsg.postValue("")
        }
    }
}

