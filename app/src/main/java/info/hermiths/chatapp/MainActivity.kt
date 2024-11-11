package info.hermiths.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.hermiths.chatapp.ui.presentation.screen.ChatScreen
import info.hermiths.chatapp.ui.theme.ChatAppTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "在线客服",
                                style = TextStyle(
                                    color = Color(0xff18243E),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.W500
                                )
                            )
                        },
                        colors = topAppBarColors(
                            containerColor = Color(0xffF3F4F6),
                            titleContentColor = Color.Black
                        ),
                        navigationIcon = {
                            IconButton(onClick = { /* do something */ }) {
                                Image(
                                    painter = painterResource(R.drawable.back),
                                    contentDescription = "Localized description",
                                    modifier = Modifier.size(width = 24.dp, height = 24.dp)
                                )
                            }
                        },
                    )
                }) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize(), color = Color(0xffF3F4F6)
                    ) {
                        ChatScreen(modifier = Modifier.padding(innerPadding))
                    }
                }

            }
        }

    }
}