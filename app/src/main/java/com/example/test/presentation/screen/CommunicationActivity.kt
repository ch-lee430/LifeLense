package com.example.test.presentation.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.test.presentation.composables.InputRow
import com.example.test.presentation.composables.MessageBubble
import com.example.test.presentation.composables.ReferenceCardSection
import com.example.test.domain.model.ReferenceType
import com.example.test.presentation.composables.CalendarDialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 추가: WindowInsetsSides.only 사용 시 필요
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommunicationActivity : ComponentActivity() {

    private val viewModel: CommunicationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedImageUri = remember { mutableStateOf<String?>(null) }
    val selectedCalendarDate = remember { mutableStateOf<Long?>(null) }
    // 캘린더 Dialog를 띄울 때 사용할 해당 월의 이벤트 목록
    val eventsInMonth = remember { mutableStateOf<List<com.example.test.domain.model.CalendarEvent>>(emptyList()) }


    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val permissionsToRequest = remember {
        val base = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CALENDAR
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            base += listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            base += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        base.toList()
    }
    val permissionState = rememberMultiplePermissionsState(permissions = permissionsToRequest)
    LaunchedEffect(Unit) { permissionState.launchMultiplePermissionRequest() }

    selectedImageUri.value?.let { uri ->
        AlertDialog(
            onDismissRequest = { selectedImageUri.value = null },
            title = { Text("사진 확대") },
            text = { Text("선택된 사진 URI: $uri\n(실제 앱에서는 이미지가 확대되어 표시됩니다)") },
            confirmButton = { Button(onClick = { selectedImageUri.value = null }) { Text("닫기") } }
        )
    }

    selectedCalendarDate.value?.let { date ->
        CalendarDialog(
            initialTimestamp = date,
            events = eventsInMonth.value, // 상태로 저장된 이벤트 목록 사용
            onClose = { selectedCalendarDate.value = null }
        )
    }

    Scaffold(
        // ✅ Scaffold의 기본 인셋 적용 끄기 (우리가 모두 수동 처리)
        contentWindowInsets = WindowInsets(0.dp),

        topBar = {
            TopAppBar(
                title = { Text("LIFELENSE (더미 테스트)", fontWeight = FontWeight.ExtraBold) },
                // ✅ TopBar는 자체 인셋 자동 적용 끄고
                windowInsets = WindowInsets(0.dp),
                // ✅ 상태바 높이만 명시적으로 소비
                modifier = Modifier.statusBarsPadding()
            )
        },

        bottomBar = {
            InputRow(
                questionText = questionText,
                onQuestionTextChange = viewModel::updateQuestionText,
                onSendClick = {
                    if (permissionState.allPermissionsGranted) {
                        viewModel.findPastMemory()
                    } else {
                        Log.e("Permission", "필수 권한이 허용되지 않았습니다. 권한 요청 재실행.")
                        permissionState.launchMultiplePermissionRequest()
                    }
                },
                isLoading = uiState is UiState.Analyzing,
                // ✅ 하단 시스템바 + IME 인셋을 **BottomBar에서만** 소비
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                ) // (선택) 좌우 시스템바 인셋
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message)

                    if (message.isPlaceholder && uiState is UiState.Analyzing) {
                        Box(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }

                    if (message.sender == Sender.GEMINI && message == messages.last() && uiState is UiState.Result) {
                        val memory = (uiState as UiState.Result).memory
                        ReferenceCardSection(
                            referenceData = memory.referenceData,
                            onCardClick = { item ->
                                when (item.type) {
                                    ReferenceType.PHOTO -> {
                                        selectedImageUri.value = item.functionalValue
                                    }
                                    ReferenceType.SMS -> {
                                        val phoneNumber = item.functionalValue.replace(Regex("[^0-9+]"), "")
                                        if (phoneNumber.isNotEmpty()) {
                                            val uri = Uri.parse("smsto:$phoneNumber")
                                            val intent = Intent(Intent.ACTION_SENDTO, uri)
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Log.e("SMS_INTENT", "메시지 앱 실행 오류: ${e.message}")
                                            }
                                        } else {
                                            Log.e("SMS_INTENT", "유효한 전화번호를 찾을 수 없습니다.")
                                        }
                                    }
                                    ReferenceType.CALL -> {
                                        // 전화는 클릭 이벤트를 비워둠
                                    }
                                    ReferenceType.CALENDAR -> {
                                        // ★★★ 캘린더 클릭 시, 전체 월 이벤트 데이터를 먼저 가져온 후 Dialog 띄우기 ★★★
                                        val clickedDate = item.date
                                        coroutineScope.launch {
                                            // 1. 해당 날짜가 속한 월의 모든 이벤트를 ViewModel을 통해 가져옴
                                            val events = viewModel.getCalendarEventsForDialog(clickedDate)
                                            // 2. 상태를 업데이트하여 Dialog를 띄움
                                            eventsInMonth.value = events
                                            selectedCalendarDate.value = clickedDate
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                if (uiState is UiState.Analyzing) {
                    item {
                        Text(
                            text = "Gemini가 답변을 준비하고 있어요...",
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------
// CardStyle & Utils
// -----------------------------------------------------------------------------------------------
data class CardStyle(
    val icon: ImageVector,
    val typeName: String,
    val iconColor: Color,
    val backgroundColor: Color
)

fun getCardStyle(type: ReferenceType): CardStyle {
    return when (type) {
        ReferenceType.PHOTO -> CardStyle(Icons.Filled.Image, "사진", Color(0xFFE5B000), Color(0xFFFFFBEB))
        ReferenceType.SMS -> CardStyle(Icons.Filled.Sms, "문자 메시지", Color(0xFF2563EB), Color(0xFFEFF6FF))
        ReferenceType.CALL -> CardStyle(Icons.Filled.Call, "통화 기록", Color(0xFFDC2626), Color(0xFFFEF2F2))
        ReferenceType.CALENDAR -> CardStyle(Icons.Filled.CalendarMonth, "캘린더 일정", Color(0xFF059669), Color(0xFFECFDF5))
    }
}

fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(timestamp))
}
