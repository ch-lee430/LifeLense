package com.example.test.presentation.communication

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunicationActivity : ComponentActivity() {

    private val viewModel: CommunicationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CommunicationScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CommunicationScreen(viewModel: CommunicationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val questionText by viewModel.questionText.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // 권한 요청 로직
    val permissionsToRequest = remember {
        val basePermissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CALENDAR
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            basePermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            basePermissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            basePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        basePermissions.toTypedArray()
    }

    // 권한 상태 관리
    val permissionState = rememberMultiplePermissionsState(permissions = permissionsToRequest.toList())
    LaunchedEffect(Unit) {
        // 앱 실행 시 권한 요청
        permissionState.launchMultiplePermissionRequest()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("과거 기억 찾기 서비스", fontWeight = FontWeight.Bold) }) },
        bottomBar = {
            // 하단에 입력창과 버튼이 위치하는 Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = viewModel::updateQuestionText,
                    label = { Text("과거에 대해 질문하세요") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (permissionState.allPermissionsGranted) {
                            viewModel.findPastMemory()
                        } else {
                            Log.e("Permission", "필수 권한이 허용되지 않았습니다.")
                        }
                    },
                    enabled = uiState != UiState.Loading && questionText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "기억 찾기")
                }
            }
        }
    ) { padding ->
        // 메시지 리스트를 표시하는 LazyColumn
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // 첫 화면의 환영 메시지
                    Text(
                        text = "🤖 Gemini에게 질문하고 과거를 되돌아보세요.",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
                items(messages) { message ->
                    MessageBubble(message)
                }
            }

            // 로딩 상태를 하단에 표시
            if (uiState is UiState.Loading) {
                Text(
                    text = "Gemini가 답변을 준비하고 있어요...",
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.sender == Sender.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color.Blue else Color.LightGray
            )
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else Color.Black,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}