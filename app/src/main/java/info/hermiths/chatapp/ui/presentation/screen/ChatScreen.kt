package info.hermiths.chatapp.ui.presentation.screen

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import info.hermiths.chatapp.R
import info.hermiths.chatapp.model.MediaMessage
import info.hermiths.chatapp.model.MessageEntity

import info.hermiths.chatapp.ui.presentation.components.MsgTypeContent
import info.hermiths.chatapp.ui.presentation.components.NavRoute
import info.hermiths.chatapp.ui.presentation.viewmodel.ChatScreenViewModel
import info.hermiths.chatapp.ui.presentation.viewmodel.ConnectionStatus
import info.hermiths.chatapp.utils.FileUtils
import java.io.File

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
                // TODO
                navController.navigate(NavRoute.MEDIA_VIEWER)
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
    navController: NavController, viewModel: ChatScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.observeAsState(ChatScreenUiState())
    val input by viewModel.inputMsg.observeAsState("init")

    val currentFocus = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    viewModel.lazyListState = lazyListState

    val mediaPermissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) listOf(
            Manifest.permission.READ_MEDIA_IMAGES
        ) else listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    )
    val hasMediaPermission = mediaPermissionState.allPermissionsGranted

    val pickFilesResult = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia(
            9
        ), onResult = { uris: List<Uri> ->
            pickFilesResult.value = uris
        })

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = { Appbar(navController) }) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(), color = Color(0xFFF3F4F6)
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // TODO connectionStatus
//        Text(text = "Connection Status: ${uiState.connectionStatus.name}")
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp),
                    color = Color(0xffEBEBEB),
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(top = 20.dp),
                    state = lazyListState
                ) {
                    itemsIndexed(uiState.messages, key = { _, msg ->
                        msg.clientMsgID
                    }) { index, message ->
                        MessageItem(
                            message = message,
                            if (message.senderUid == ChatScreenViewModel.uid) MessagePosition.RIGHT
                            else MessagePosition.LEFT,
                            viewModel,
                            navController
                        )
                        LaunchedEffect(uiState.messages) {
                            Log.d("列表滑动", "index: $index")
                            if (lazyListState.isScrollInProgress) {
                                // TODO 待优化
                                if (uiState.messages.size - index > ChatScreenViewModel.showPageSize - 3) {
                                    if (ChatScreenViewModel.currentPage > 1) {
                                        ChatScreenViewModel.currentPage -= 1
                                        Log.d(
                                            "列表滑动",
                                            "分页时的 old index: $index, msgs size: ${uiState.messages.size}, msg: ${uiState.messages[index].msgBody}"
                                        )
                                        viewModel.filterLocalMessages(
                                            send = false,
                                            needScrollEnd = false,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White, modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    ) {
                        Image(painter = painterResource(R.drawable.open_file),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(5.dp)
                                .size(21.dp, 16.dp)
                                .clickable {
                                    if (hasMediaPermission) {
                                        launcher.launch(
                                            PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    } else {
                                        mediaPermissionState
                                            .launchMultiplePermissionRequest()
                                            .let {
                                                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                            }
                                    }
//                            open mime type
//                            val mimeType = "image/gif"
//                            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.SingleMimeType(mimeType)))
                                })
                        LaunchedEffect(
                            pickFilesResult.value
                        ) {
                            if (pickFilesResult.value.isNotEmpty()) {
                                val uris = pickFilesResult.value
                                Log.d(ChatScreenViewModel.FILESELECT, "${pickFilesResult.value}")
                                for (uri in uris) {
                                    val cr = context.contentResolver
                                    val projection = arrayOf(
                                        MediaStore.MediaColumns.DATA,
                                        MediaStore.MediaColumns.MIME_TYPE,
                                        MediaStore.MediaColumns.WIDTH,
                                        MediaStore.MediaColumns.HEIGHT
                                    )
                                    val metaCursor = cr.query(uri, projection, null, null, null)
                                    metaCursor?.use { mCursor ->
                                        if (mCursor.moveToFirst()) {
                                            val path = mCursor.getString(0)
                                            val mime = mCursor.getString(1)
                                            val width = mCursor.getInt(2)
                                            val height = mCursor.getInt(3)
                                            Log.d(
                                                ChatScreenViewModel.FILESELECT,
                                                "path: $path, width: $width, height: $height"
                                            )
                                            val file = File(path)
                                            val mediaMessage = MediaMessage(
                                                width = width,
                                                height = height,
                                                file = file,
                                                isImage = FileUtils.isImage(mime),
                                            )
                                            viewModel.upload(mediaMessage)
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

                    Spacer(modifier = Modifier.width(12.dp))

                    BasicTextField(value = input,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = viewModel::onMessageChange,
                        maxLines = 5,
                        decorationBox = { innerTextField ->
                            Surface(
                                color = Color.White,
                                modifier = Modifier
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
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
                                                viewModel.sendMessageFromInput(messageSent = {
                                                    currentFocus.clearFocus()
                                                })
                                            })
                                }
                            }
                        })
                }
            }
        }

        AnimatedVisibility(visible = !uiState.login) {
            NickIdPrompt { nid, nName, lbeIdentity ->
                viewModel.setNickId(nid, nName, lbeIdentity)
            }
        }
    }
}

@Composable
fun NickIdPrompt(onStart: (nid: String, nName: String, lbeIdentity: String) -> Unit) {
    // HermitK15
    var nickId by remember { mutableStateOf("HermitK17") }
    var nickName by remember { mutableStateOf("HermitK17") }
    // dev
//     var lbeIdentity by remember { mutableStateOf("42nz10y3hhah") }
    // sit
    var lbeIdentity by remember { mutableStateOf("43p28i7bt9l6") }

    Dialog(onDismissRequest = { }) {
        Card {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(value = nickId,
                    onValueChange = { nickId = it },
                    label = { Text(text = "NickId") })
                OutlinedTextField(value = nickName,
                    onValueChange = { nickName = it },
                    label = { Text(text = "NickName") })
                OutlinedTextField(value = lbeIdentity,
                    onValueChange = { lbeIdentity = it },
                    label = { Text(text = "LbeIdentity") })
                Button(onClick = { onStart(nickId, nickName, lbeIdentity) }) {
                    Text(text = "Connect")
                }
            }
        }
    }
}


@Composable
fun MessageItem(
    message: MessageEntity,
    messagePosition: MessagePosition,
    viewModel: ChatScreenViewModel,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (messagePosition == MessagePosition.LEFT) 0.dp else 24.dp,
                end = if (messagePosition == MessagePosition.RIGHT) 0.dp else 24.dp
            ),
        contentAlignment = if (messagePosition == MessagePosition.LEFT) Alignment.TopStart else Alignment.BottomEnd
    ) {
        if (messagePosition == MessagePosition.LEFT) {
            RecievFromCs(message, messagePosition, viewModel = viewModel, navController)
        } else {
            UserInput(message, messagePosition, viewModel = viewModel, navController)
        }
    }
}

@Composable
fun RecievFromCs(
    message: MessageEntity,
    messagePosition: MessagePosition,
    viewModel: ChatScreenViewModel,
    navController: NavController
) {
    Row {
        // avatar
        Image(
            painter = painterResource(id = R.drawable.cs_avatar),
            contentDescription = "",
            modifier = Modifier.size(32.dp)
        )

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message.senderUid,
                modifier = Modifier.align(if (messagePosition == MessagePosition.LEFT) Alignment.Start else Alignment.End),
                style = TextStyle(
                    fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff979797)
                )
            )
            Spacer(Modifier.height(8.dp))
            MsgTypeContent(message, viewModel, navController, false)
        }
    }
}

@Composable
fun UserInput(
    message: MessageEntity,
    messagePosition: MessagePosition,
    viewModel: ChatScreenViewModel,
    navController: NavController
) {
    Row(horizontalArrangement = Arrangement.End) {
        Column(
            horizontalAlignment = Alignment.End, modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message.senderUid,
                modifier = Modifier.align(if (messagePosition == MessagePosition.LEFT) Alignment.Start else Alignment.End),
                style = TextStyle(
                    fontSize = 14.sp, fontWeight = FontWeight.W400, color = Color(0xff979797)
                )
            )
            Spacer(Modifier.height(8.dp))
            MsgTypeContent(message, viewModel, navController, true)
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

//        SubcomposeAsyncImage(
//            model = "https://k.sinaimg.cn/n/sinakd20117/0/w800h800/20240127/889b-4c8a7876ebe98e4d619cdaf43fceea7c.jpg/w700d1q75cms.jpg",
//            contentDescription = "",
//            contentScale = ContentScale.FillBounds,
//            modifier = Modifier
//                .size(32.dp)
//                .clip(CircleShape),
//            loading = {
//                CircularProgressIndicator()
//            },
//            onLoading = { loading ->
//
//            },
//        )
    }
}





