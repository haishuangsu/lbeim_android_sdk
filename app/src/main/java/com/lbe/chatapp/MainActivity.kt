package com.lbe.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lbe.imsdk.LbeSdk
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.lbeIdentity
import com.lbe.imsdk.ui.presentation.viewmodel.ChatScreenViewModel.Companion.lbeSign
import com.lbe.imsdk.ui.theme.ChatAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            ChatAppTheme {
                NickIdPrompt { nickId, nickName, lbeIdentity, lbeSign, phone, email, language, device, headerIcon ->
                    LbeSdk.init(
                        context = context,
                        lbeSign = lbeSign,
                        lbeIdentity = lbeIdentity,
                        nickId = nickId,
                        nickName = nickName,
                        phone = phone,
                        email = email,
                        language = language,
                        device = device,
                        headerIcon = headerIcon
                    )
                    finish()
                }
            }
        }
    }
}

@Composable
fun NickIdPrompt(onStart: (nickId: String, nickName: String, lbeIdentity: String, lbeSign: String, phone: String, email: String, language: String, device: String, headerIcon: String) -> Unit) {
    // HermitK1
    var nickId by remember { mutableStateOf("android0157") }
    var nickName by remember { mutableStateOf("android0157") }

    // dev
//    var lbeSign by remember { mutableStateOf("0xaee7b220061d450ef94406f819edec6f9402a1a41205e75e394519c02a527e3d1527e166ea6c868075b094d3bddbb1b274ccef66dd247ebec6930276f361088b1b") }
//    var lbeIdentity by remember { mutableStateOf("441z9t7ucki1") }

    // sit
//    var lbeSign by remember { mutableStateOf("0x9d63fcec00dffa1e7bbebfa4f0afa80f5f26614613b29357d580b69b708d2d893b6eef2d013828830f9c52f647edcd9ebc5ec73900d178b4c1a27732fb24cefe1b") }
//    var lbeIdentity by remember { mutableStateOf("441zy52mn2yy") }

    // uat
    // var lbeSign by remember { mutableStateOf("0xc3620a07c69a191b3b2fb431bd26c8417413f4998e6fc2fc2c570bd1145ac004780d13db14290687a94d9b30804b1a8a2edb8b7828c6c45a55fc8d1e78f98dec1c") }
    // var lbeIdentity by remember { mutableStateOf("45vhxodzxswp") }

    // test
    var lbeSign by remember { mutableStateOf("0xad1701e4fd5456c87541a6bb5ccd41ae626d2a0bd52b6ff7fa78b7276632b5ff47386d2ed0cafa53ffb7364880c7e30a7e3688b6efc59a1ba9cda2f4216d2e9c1b") }
    var lbeIdentity by remember { mutableStateOf("43qf47gjuimi") }
//    android015

    var phone by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var language by remember { mutableStateOf("zh") }

    var device by remember { mutableStateOf("") }

//    var headerIcon by remember { mutableStateOf("https://k.sinaimg.cn/n/sinakd20117/0/w800h800/20240127/889b-4c8a7876ebe98e4d619cdaf43fceea7c.jpg/w700d1q75cms.jpg") }
    var headerIcon by remember { mutableStateOf("http://10.40.92.203:9910/openimttt/lbe_65f8d397953b979b4be0d098e8d4f5.jpg") }

//    var source by remember { mutableStateOf("Android") }

    Card {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                OutlinedTextField(
                    value = lbeSign,
                    onValueChange = { lbeSign = it },
                    label = { Text(text = "lbeSign") },
                    readOnly = false,
                )
            }

            item {
                OutlinedTextField(
                    value = lbeIdentity,
                    onValueChange = { lbeIdentity = it },
                    label = { Text(text = "LbeIdentity") }, readOnly = false,
                )
            }

            item {
                OutlinedTextField(
                    value = nickId,
                    onValueChange = { nickId = it },
                    label = { Text(text = "NickId") }, readOnly = false,
                )
            }

            item {
                OutlinedTextField(
                    value = nickName,
                    onValueChange = { nickName = it },
                    label = { Text(text = "NickName") }, readOnly = false,
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(text = "phone") }, readOnly = false,
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = "email") }, readOnly = false,
                )
            }

            item {
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text(text = "language") }, readOnly = false,
                )
            }
            item {
                OutlinedTextField(
                    value = device,
                    onValueChange = { device = it },
                    label = { Text(text = "device") }, readOnly = false,
                )
            }
            item {
                OutlinedTextField(
                    value = headerIcon,
                    onValueChange = { headerIcon = it },
                    label = { Text(text = "avatar") }, readOnly = false,
                )
            }

            item {
                Button(onClick = {
                    onStart(
                        nickId,
                        nickName,
                        lbeIdentity,
                        lbeSign,
                        phone,
                        email,
                        language,
                        device,
                        headerIcon
                    )
                }) {
                    Text(text = "Connect")
                }
            }
        }
    }

}