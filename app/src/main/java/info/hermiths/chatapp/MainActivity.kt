package info.hermiths.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import info.hermiths.lbesdk.LbeChatActivity
import info.hermiths.lbesdk.model.InitArgs
import info.hermiths.lbesdk.ui.theme.ChatAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            ChatAppTheme {
                NickIdPrompt { nid, nName, lbeIdentity ->
                    val initArgs = InitArgs(
                        lbeSign = "b184b8e64c5b0004c58b5a3c9af6f3868d63018737e68e2a1ccc61580afbc8f112119431511175252d169f0c64d9995e5de2339fdae5cbddda93b65ce305217700",
                        nickId = nid,
                        nickName = nName,
                        lbeIdentity = lbeIdentity
                    )
                    val intent = Intent(context, LbeChatActivity::class.java).putExtra(
                        "initArgs", Gson().toJson(initArgs)
                    )
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun NickIdPrompt(onStart: (nid: String, nName: String, lbeIdentity: String) -> Unit) {
    // HermitK15
    var nickId by remember { mutableStateOf("HermitK1") }
    var nickName by remember { mutableStateOf("HermitK1") }

    // dev: 42nz10y3hhah; faq: 43hw3seddn2i
//    var lbeIdentity by remember { mutableStateOf("43hw3seddn2i") }

    // sit: my: 441zy52mn2yy
    var lbeIdentity by remember { mutableStateOf("441zy52mn2yy") }

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