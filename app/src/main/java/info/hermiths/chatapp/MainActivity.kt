package info.hermiths.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import info.hermiths.lbesdk.ui.theme.ChatAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            ChatAppTheme {
                NickIdPrompt { nickId, nickName, lbeIdentity, lbeSign, phone, email, language, device ->
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
                    )
                    finish()
                }
            }
        }
    }
}

@Composable
fun NickIdPrompt(onStart: (nickId: String, nickName: String, lbeIdentity: String, lbeSign: String, phone: String, email: String, language: String, device: String) -> Unit) {
    // HermitK15
    // HermitK1, sit
    var nickId by remember { mutableStateOf("HermitK2") }
    var nickName by remember { mutableStateOf("HermitK2") }

    // dev: 42nz10y3hhah; faq: 43hw3seddn2i
//    var lbeIdentity by remember { mutableStateOf("43hw3seddn2i") }

    // sit: my: 441zy52mn2yy
    var lbeIdentity by remember { mutableStateOf("441zy52mn2yy") }

    var lbeSign by remember { mutableStateOf("b184b8e64c5b0004c58b5a3c9af6f3868d63018737e68e2a1ccc61580afbc8f112119431511175252d169f0c64d9995e5de2339fdae5cbddda93b65ce305217700") }

    var phone by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var language by remember { mutableStateOf("zh") }

    var device by remember { mutableStateOf("") }

//    var source by remember { mutableStateOf("Android") }

    Card {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(value = lbeSign,
                onValueChange = { lbeSign = it },
                label = { Text(text = "lbeSign") })

            OutlinedTextField(value = lbeIdentity,
                onValueChange = { lbeIdentity = it },
                label = { Text(text = "LbeIdentity") })

            OutlinedTextField(value = nickId,
                onValueChange = { nickId = it },
                label = { Text(text = "NickId") })

            OutlinedTextField(value = nickName,
                onValueChange = { nickName = it },
                label = { Text(text = "NickName") })

            OutlinedTextField(value = phone,
                onValueChange = { phone = it },
                label = { Text(text = "phone") })
            OutlinedTextField(value = email,
                onValueChange = { email = it },
                label = { Text(text = "email") })

            OutlinedTextField(value = language,
                onValueChange = { language = it },
                label = { Text(text = "language") })

            OutlinedTextField(value = device,
                onValueChange = { device = it },
                label = { Text(text = "device") })
//                item {
//                    OutlinedTextField(value = source,
//                        onValueChange = { source = it },
//                        label = { Text(text = "source") })
//                }
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
//                            source
                )
            }) {
                Text(text = "Connect")
            }

        }
    }

}