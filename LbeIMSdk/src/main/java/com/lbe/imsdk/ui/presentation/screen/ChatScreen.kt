package com.lbe.imsdk.ui.presentation.screen

import LightPullToRefreshList
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.lbe.imsdk.R
import com.lbe.imsdk.model.MediaMessage
import com.lbe.imsdk.model.MessageEntity

import com.lbe.imsdk.ui.presentation.components.MsgTypeContent
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel
import com.lbe.imsdk.ui.presentation.viewmodel.ConnectionStatus
import com.lbe.imsdk.utils.FileUtils
import com.lbe.imsdk.utils.TimeUtils
import java.io.File
import java.util.Date

data class ChatScreenUiState(
    var messages: List<MessageEntity> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.NOT_STARTED,
    val login: Boolean = false,
)

enum class MessagePosition {
    LEFT, RIGHT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Appbar(navController: NavController) {
    val ctx = LocalContext.current

    CenterAlignedTopAppBar(
        title = {
            Text(
                "在线客服", style = TextStyle(
                    color = Color(0xff18243E), fontSize = 18.sp, fontWeight = FontWeight.W500
                )
            )
        },
        colors = topAppBarColors(
            containerColor = Color(0xFFF3F4F6), titleContentColor = Color.Black
        ),
        navigationIcon = {
            IconButton(onClick = {
                if (ctx is Activity) {
                    ctx.finish()
                }
            }) {
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Localized description",
                    modifier = Modifier.size(width = 24.dp, height = 24.dp)
                )
            }
        },
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    navController: NavController, viewModel: ChatScreenViewModel, imageLoader: ImageLoader
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val uiState by viewModel.uiState.observeAsState(ChatScreenUiState())

//    val messages by viewModel.messageList.collectAsState()

    val input by viewModel.inputMsg.observeAsState("init")
    var showDialog by remember { mutableStateOf(false) }

    val currentFocus = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
//    val coroutineScope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()
    viewModel.lazyListState = lazyListState

    val pickFilesResult = remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia(
            9
        ), onResult = { uris: List<Uri> ->
            pickFilesResult.value = uris
        })

    val mediaPermissionState = rememberMultiplePermissionsState(
        permissions = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
        ) else listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    ) {
        launcher.launch(
            PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo)
        )
    }

    val hasMediaPermission = mediaPermissionState.allPermissionsGranted

    val showToBottomButton by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex < uiState.messages.size - 10
        }
    }

    val isConnected by viewModel.isConnected.observeAsState(initial = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { Appbar(navController) }) { innerPadding ->
        Surface(color = Color(0xFFF3F4F6), modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    currentFocus.clearFocus()
                }
            }) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isConnected) {
                    Surface(
                        color = Color(0xffFF6164).copy(0.1f), modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.network_unavailable),
                                contentDescription = "",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "网络不可用，请检查你的网络",
                                style = TextStyle(
                                    color = Color(0xff979797),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W400
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
                            )
                        }
                    }
                } else {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp),
                        color = Color(0xffEBEBEB),
                    )
                }

                LightPullToRefreshList(
                    modifier = Modifier.weight(1f),
                    listState = lazyListState,
                    onRefresh = {
                        if (ChatScreenViewModel.currentPage > 1) {
                            ChatScreenViewModel.currentPage -= 1
                            viewModel.filterLocalMessages()
                        } else {
                            viewModel.loadHistory()
                        }

                    },
                    lazyColumn = {
                        LaunchedEffect(lazyListState.isScrollInProgress) {
                            if (lazyListState.isScrollInProgress) {
                                currentFocus.clearFocus()
                                keyboardController?.hide()
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(top = 20.dp),
                            state = lazyListState
                        ) {
                            itemsIndexed(
                                uiState.messages,
                                key = { _, msg ->
                                    msg.clientMsgID
                                },
                            ) { index, message ->
                                MessageItem(
                                    uiState.messages,
                                    message = message,
                                    if (message.senderUid == ChatScreenViewModel.uid) MessagePosition.RIGHT
                                    else MessagePosition.LEFT,
                                    viewModel,
                                    navController,
                                    imageLoader,
                                )

                                LaunchedEffect(uiState.messages) {
                                    if (index <= uiState.messages.size - 1) {
                                        val visitAbleMsg = uiState.messages[index]
//                                        Log.d(
//                                            "列表滑动",
//                                            "VisitAble Entry --->>  index: $index, clientMsgID || ${visitAbleMsg.clientMsgID} || msg: ${visitAbleMsg.msgBody} || readed: ${visitAbleMsg.readed} "
//                                        )
                                        if (!visitAbleMsg.readed && visitAbleMsg.senderUid != ChatScreenViewModel.uid) {
                                            viewModel.markRead(message)
                                        }

//                                        if (lazyListState.isScrollInProgress) {
//                                            currentFocus.clearFocus()
//                                            keyboardController?.hide()
//                                        }
                                    }
                                }
                            }
                        }
                    },
                )

                var isExpanded by remember { mutableStateOf(false) }
                var lineCount by remember { mutableIntStateOf(1) }
                var textFieldHeight by remember { mutableStateOf(42.dp) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 14.dp, end = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.height(textFieldHeight)
                    ) {
                        if (isExpanded) {
                            Surface(color = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.TopStart)
                                    .clickable {
                                        showDialog = true
                                    }) {
                                Image(
                                    painter = painterResource(R.drawable.expanded),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .size(8.dp)
                                )
                            }
                        }

                        Surface(color = Color.White,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .align(Alignment.BottomStart)
                                .clickable {
                                    if (!hasMediaPermission) {
                                        mediaPermissionState.launchMultiplePermissionRequest()
                                    } else {
                                        launcher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                    }
                                }) {
                            Image(
                                painter = painterResource(R.drawable.open_file),
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(5.dp)
                                    .size(21.dp, 16.dp)
                            )
                            LaunchedEffect(
                                pickFilesResult.value
                            ) {
                                if (pickFilesResult.value.isNotEmpty()) {
                                    val uris = pickFilesResult.value
                                    Log.d(
                                        ChatScreenViewModel.FILESELECT, "${pickFilesResult.value}"
                                    )
                                    for (uri in uris) {
                                        val cr = context.contentResolver
                                        cr.takePersistableUriPermission(
                                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                        val projection = arrayOf(
                                            MediaStore.MediaColumns.DATA,
                                            MediaStore.MediaColumns.MIME_TYPE,
                                        )
                                        val metaCursor = cr.query(uri, projection, null, null, null)
                                        metaCursor?.use { mCursor ->
                                            if (mCursor.moveToFirst()) {
                                                val path = mCursor.getString(0)
                                                val mime = mCursor.getString(1)
                                                val file = File(path)
                                                val mediaMessage = MediaMessage(
                                                    width = 1080,
                                                    height = 1920,
                                                    file = file,
                                                    path = uri.toString(),
                                                    mime = mime,
                                                    isImage = FileUtils.isImage(mime),
                                                )
                                                viewModel.preInsertUpload(mediaMessage)
                                                Log.d(
                                                    ChatScreenViewModel.FILESELECT,
                                                    "found file --->> ${file.name}, ${file.path}, ${file.length()}, ${file.absolutePath}, mimeType: $mime, Is image file: ${
                                                        FileUtils.isImage(
                                                            mime
                                                        )
                                                    }"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val maxLength = 500
                    if (showDialog) {
                        Dialog(
                            onDismissRequest = { showDialog = false },
                            properties = DialogProperties(usePlatformDefaultWidth = false)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = MaterialTheme.shapes.medium,
                                color = Color(0xFFF3F4F6)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(color = Color.White,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    showDialog = false
                                                }) {
                                            Image(
                                                painter = painterResource(R.drawable.close),
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .size(8.dp)
                                            )
                                        }

                                        Text(
                                            "${input.length}/$maxLength", style = TextStyle(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.W400,
                                                color = Color(0xff979797)
                                            )
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    BasicTextField(
                                        value = input, onValueChange = { newValue ->
                                            if (newValue.length <= maxLength) {
                                                viewModel.onMessageChange(newValue)
                                            }
                                        }, modifier = Modifier.fillMaxSize(), textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W400,
                                            color = Color.Black,
                                        )
                                    )
                                }
                            }
                        }
                    }

                    println("动态 TextField --->> isExpanded: $isExpanded")
                    BasicTextField(
                        value = input,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        onValueChange = { newValue ->
                            if (newValue.length <= maxLength) {
                                viewModel.onMessageChange(newValue)
                            }
                        },
                        maxLines = 5,
                        onTextLayout = { textLayoutResult ->
                            lineCount = textLayoutResult.lineCount
                            println("动态 TextField --->> lineCount: $lineCount")
                            isExpanded = lineCount > 4

                            if (!isExpanded) {
                                if (lineCount < 2) {
                                    textFieldHeight = 42.dp
                                } else {
                                    val calculatedHeightPx = textLayoutResult.size.height
                                    textFieldHeight = with(density) {
                                        calculatedHeightPx.toDp().plus(23.dp).coerceAtMost(203.dp)
                                    }
                                }
                            }
                        },
                        decorationBox = { innerTextField ->
                            Surface(
                                color = Color.White,
                                modifier = Modifier
                                    .heightIn(min = 42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 12.dp, end = 12.dp, top = 9.dp, bottom = 11.dp
                                        ),
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    Box(Modifier.weight(1f)) {
                                        if (input.isEmpty()) Text(
                                            "请输入你想咨询的问题", style = TextStyle(
                                                color = Color(0xffEBEBEB),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.W400,
                                            )
                                        )
                                        innerTextField()
                                    }

                                    Image(painter = painterResource(R.drawable.send),
                                        contentDescription = "Send Button",
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                viewModel.sendMessageFromTextInput(messageSent = {
                                                    currentFocus.clearFocus()
                                                })
                                            })
                                }
                            }
                        },
                    )
                }

            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 59.dp)
            ) {
                timeoutTips(viewModel = viewModel)
            }
        }

        AnimatedVisibility(visible = showToBottomButton, enter = fadeIn(), exit = fadeOut()) {
            ToBottom(viewModel = viewModel, goToTop = {
                viewModel.scrollToBottom()
                viewModel.resetRecivCount()
            })

        }
    }
}

@Composable
fun timeoutTips(viewModel: ChatScreenViewModel) {
    val timeoutVisibility by viewModel.isTimeOut.collectAsState()
    AnimatedVisibility(visible = timeoutVisibility) {
        Log.d("TimeOut", "timeoutVisibility --->>> $timeoutVisibility")
        Column {
            Surface(
                color = Color(0xffEBEBEB), modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "您已超过${viewModel.timeOut}分钟未回复",
                    style = TextStyle(
                        color = Color(0xff979797), fontSize = 12.sp, fontWeight = FontWeight.W400
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ToBottom(viewModel: ChatScreenViewModel, goToTop: () -> Unit) {
    val recivCount by viewModel.recivCount.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(color = Color(0xff0054FC).copy(alpha = 0.15f),
            modifier = Modifier
                .padding(bottom = 109.dp, end = 16.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 12.dp,
                    )
                )
                .clickable {
                    goToTop()
                }
//                .blur(20.dp)
                .align(Alignment.BottomEnd)) {
            Row(
                modifier = Modifier.padding(
                    start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp
                )
            ) {
                Text(
                    if (recivCount == 0) "回到底部" else "有${recivCount}条新消息",
                    style = TextStyle(
                        color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.W400
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.to_bottom),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MessageItem(
    messages: List<MessageEntity>,
    message: MessageEntity,
    messagePosition: MessagePosition,
    viewModel: ChatScreenViewModel,
    navController: NavController,
    imageLoader: ImageLoader,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        contentAlignment = if (messagePosition == MessagePosition.LEFT) Alignment.TopStart else Alignment.BottomEnd
    ) {
        if (messagePosition == MessagePosition.LEFT) {
            RecievFromCs(
                messages,
                message,
                messagePosition,
                viewModel = viewModel,
                navController,
                imageLoader
            )
        } else {
            UserInput(
                messages,
                message,
                messagePosition,
                viewModel = viewModel,
                navController,
                imageLoader
            )
        }
    }
}

@Composable
fun RecievFromCs(
    messages: List<MessageEntity>,
    message: MessageEntity,
    messagePosition: MessagePosition,
    viewModel: ChatScreenViewModel,
    navController: NavController,
    imageLoader: ImageLoader,
) {
    val currentIndex = messages.indexOf(message)
    var needShowTime = false
    var sameCurrentDay = false
    if (currentIndex != 0) {
        val prev = messages[currentIndex - 1]
//        Log.d("History time", "currentIndex: $currentIndex, sendTime: ${message.sendTime}, ${message.clientMsgID};  prev sendTime: ${prev.sendTime}")
        val diff = (message.sendTime - prev.sendTime) / 1000
        sameCurrentDay = TimeUtils.isSameDay(Date(), Date(message.sendTime))
//        Log.d("History time", "index: $currentIndex, 距离上条消息时间: $diff s")
        if (diff > 60 * 3) {
            needShowTime = true
//            Log.d("History time", "currentIndex: $currentIndex, 超过3分钟显示")
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = needShowTime) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (sameCurrentDay) TimeUtils.formatHHMMTime(message.sendTime) else TimeUtils.formatYYMMHHMMTime(
                        message.sendTime
                    ),
                    style = TextStyle(
                        color = Color(0xff979797), fontSize = 10.sp, fontWeight = FontWeight.W400
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 9.dp)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Image(
                painter = painterResource(id = R.drawable.cs_avatar),
                contentDescription = "",
                modifier = Modifier.size(32.dp)
            )

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.senderUid.ifEmpty { "客服机器人" },
                    modifier = Modifier.align(if (messagePosition == MessagePosition.LEFT) Alignment.Start else Alignment.End),
                    style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff979797)
                    )
                )
                Spacer(Modifier.height(8.dp))
                MsgTypeContent(message, viewModel, navController, false, imageLoader)
            }
        }
    }
}

@Composable
fun UserInput(
    messages: List<MessageEntity>,
    message: MessageEntity,
    messagePosition: MessagePosition,
    viewModel: ChatScreenViewModel,
    navController: NavController,
    imageLoader: ImageLoader,
) {
    val currentIndex = messages.indexOf(message)
    var needShowTime = false
    var sameCurrentDay = false
    if (currentIndex != 0) {
        val prev = messages[currentIndex - 1]
        val diff = (message.sendTime - prev.sendTime) / 1000
        sameCurrentDay = TimeUtils.isSameDay(Date(), Date(message.sendTime))
        if (diff > 60 * 3) {
            needShowTime = true
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(visible = needShowTime) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (sameCurrentDay) TimeUtils.formatHHMMTime(message.sendTime) else TimeUtils.formatYYMMHHMMTime(
                        message.sendTime
                    ),
                    style = TextStyle(
                        color = Color(0xff979797), fontSize = 10.sp, fontWeight = FontWeight.W400
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 9.dp)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
            ) {
                Text(
                    text = message.senderUid,
                    modifier = Modifier.align(if (messagePosition == MessagePosition.LEFT) Alignment.Start else Alignment.End),
                    style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff979797)
                    )
                )
                Spacer(Modifier.height(8.dp))
                MsgTypeContent(message, viewModel, navController, true, imageLoader)
            }

            // avatar
            AsyncImage(
                model = "https://k.sinaimg.cn/n/sinakd20117/0/w800h800/20240127/889b-4c8a7876ebe98e4d619cdaf43fceea7c.jpg/w700d1q75cms.jpg",
                contentDescription = "Yo",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
            )
        }
    }
}





