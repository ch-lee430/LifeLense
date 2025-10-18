package com.example.test.presentation.dummy

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

// =================================================================================================
// 1. DUMMY ACTIVITY & SCREEN
// =================================================================================================

@AndroidEntryPoint
class DummyActivity : ComponentActivity() {

    private val viewModel: DummyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DummyScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DummyScreen(viewModel: DummyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val questionText by viewModel.questionText.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val listState = rememberLazyListState()
    val context = LocalContext.current // 인텐트 실행을 위해 Context 가져오기

    // ★★★ 클릭 이벤트 상태 변수 정의 ★★★
    val selectedImageUri = remember { mutableStateOf<String?>(null) }
    val selectedCalendarDate = remember { mutableStateOf<Long?>(null) }

    val dummyEvents = remember {
        // UseCase에서 임시 이벤트 목록을 가져와서 사용
        viewModel.dummyMemoryUseCase.getEventsByTimestamp(System.currentTimeMillis() - 1000)
    }

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
        basePermissions.toList()
    }
    val permissionState = rememberMultiplePermissionsState(permissions = permissionsToRequest)
    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    // ★★★ 다이얼로그 (팝업) 표시 로직 ★★★
    selectedImageUri.value?.let { uri ->
        // ImageDialog(uri = uri, onClose = { selectedImageUri.value = null }) // 실제 구현 시
        AlertDialog(
            onDismissRequest = { selectedImageUri.value = null },
            title = { Text("사진 확대") },
            text = { Text("선택된 사진 URI: $uri\n(실제 앱에서는 이미지가 확대되어 표시됩니다)") },
            confirmButton = { Button(onClick = { selectedImageUri.value = null }) { Text("닫기") } }
        )
    }

    selectedCalendarDate.value?.let { date ->
        // 캘린더 Dialog 호출: 클릭된 날짜와 더미 이벤트 목록을 전달
        CalendarDialog(
            initialTimestamp = date,
            events = dummyEvents,
            onClose = { selectedCalendarDate.value = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LIFELENSE (더미 테스트)", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) }
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
                isLoading = uiState is UiState.Analyzing
            )
        }
    ) { padding ->
        // LazyColumn이 Scaffold의 모든 패딩과 IME 인셋을 직접 처리
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // Top 패딩만 명시적으로 적용. Bottom 패딩은 contentPadding으로 관리
                .padding(top = padding.calculateTopPadding())
                // 키보드가 올라올 때 스크롤 영역을 확보
                .padding(horizontal = 16.dp),

            state = listState,

            // ★★★ 핵심 수정: Bottom Content Padding에 InputRow 높이 + 추가 마진을 명시 ★★★
            // 기존 코드: vertical = 8.dp (top: 8.dp, bottom: 8.dp) + 명시적 bottom 계산
            contentPadding = PaddingValues(
                start = 0.dp, // 혹은 16.dp를 LazyColumn 외부로 빼지 않았다면 16.dp
                top = 8.dp,   // vertical의 상단 값
                end = 0.dp,   // 혹은 16.dp를 LazyColumn 외부로 빼지 않았다면 16.dp
                // bottom은 InputRow 높이 + 8.dp
                bottom = padding.calculateBottomPadding() + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message)

                if (message.isPlaceholder && uiState is UiState.Analyzing) {
                    Box(modifier = Modifier.padding(start = 12.dp, top = 4.dp)) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }

                // Gemini 답변 메시지 바로 아래에 Reference Card 섹션 삽입
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
                                    // 캘린더 클릭 시, 해당 이벤트의 날짜 타임스탬프를 Dialog 상태에 저장
                                    selectedCalendarDate.value = item.date
                                }
                            }
                        }
                    )
                }
            }

            // 로딩 상태 텍스트 (LazyColumn의 마지막 아이템으로 추가)
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

// CardStyle 정의 (public으로 설정)
data class CardStyle(
    val icon: ImageVector,
    val typeName: String,
    val iconColor: Color,
    val backgroundColor: Color
)

// getCardStyle 함수 (public으로 설정)
fun getCardStyle(type: ReferenceType): CardStyle {
    return when (type) {
        ReferenceType.PHOTO -> CardStyle(Icons.Filled.Image, "사진", Color(0xFFE5B000), Color(0xFFFFFBEB)) // 노랑 계열
        ReferenceType.SMS -> CardStyle(Icons.Filled.Sms, "문자 메시지", Color(0xFF2563EB), Color(0xFFEFF6FF)) // 파랑 계열
        ReferenceType.CALL -> CardStyle(Icons.Filled.Call, "통화 기록", Color(0xFFDC2626), Color(0xFFFEF2F2)) // 빨강 계열
        ReferenceType.CALENDAR -> CardStyle(Icons.Filled.CalendarMonth, "캘린더 일정", Color(0xFF059669), Color(0xFFECFDF5)) // 녹색 계열
    }
}

// 날짜 포맷팅 유틸리티
fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(timestamp))
}
