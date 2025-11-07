package com.example.test.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.test.domain.model.CalendarEvent
import com.example.test.presentation.screen.formatTimestamp
import java.text.SimpleDateFormat
import java.util.*

// 날짜를 요일의 시작으로 맞추기 위한 유틸리티
private fun startOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

@Composable
fun CalendarDialog(
    initialTimestamp: Long,
    events: List<CalendarEvent>, // 해당 월의 모든 이벤트 목록
    onClose: () -> Unit
) {
    val initialDate = Date(initialTimestamp)
    val calendar = Calendar.getInstance().apply { time = initialDate }
    val currentMonthYear = remember { SimpleDateFormat("yyyy년 MM월", Locale.getDefault()).format(initialDate) }

    // ★★★ 선택된 날짜와 해당 날짜의 이벤트를 관리하는 상태 ★★★
    val selectedDateTimestamp = remember { mutableStateOf(startOfDay(initialTimestamp)) }
    val selectedEvents = remember {
        mutableStateOf(events.filter { startOfDay(it.date) == selectedDateTimestamp.value })
    }

    // selectedDateTimestamp가 변경될 때마다 selectedEvents 업데이트
    LaunchedEffect(selectedDateTimestamp.value) {
        selectedEvents.value = events.filter { startOfDay(it.date) == selectedDateTimestamp.value }
    }


    // 날짜를 1일부터 마지막 날까지 생성 (간단한 더미 달력)
    val daysInMonth = (1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).toList()
    val dayLabels = listOf("일", "월", "화", "수", "목", "금", "토")

    // 이벤트를 맵으로 변환하여 O(1) 검색 가능하게 함
    val eventDates = remember { events.map { startOfDay(it.date) }.toSet() }

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.width(360.dp),
        title = { Text("기록 날짜 달력") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 1. 월/년도 표시
                Text(currentMonthYear, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                // 2. 달력 그리드
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 요일 표시
                    items(dayLabels.size) { index ->
                        Text(dayLabels[index], textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, color = if (index == 0) Color.Red else Color.Black)
                    }

                    // 날짜 빈 공간 채우기 (해당 월의 첫 번째 요일 맞추기)
                    val tempCalendar = Calendar.getInstance().apply {
                        timeInMillis = initialTimestamp
                        set(Calendar.DAY_OF_MONTH, 1) // 1일로 설정
                    }
                    val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) // 1=일요일, 7=토요일
                    val leadingBlanks = firstDayOfWeek - 1
                    items(leadingBlanks) { Spacer(Modifier.size(36.dp)) }

                    // 날짜 숫자 표시
                    items(daysInMonth.size) { index ->
                        val dayOfMonth = daysInMonth[index]
                        val dayCalendar = Calendar.getInstance().apply {
                            timeInMillis = initialTimestamp
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        }
                        val dayTimestamp = startOfDay(dayCalendar.timeInMillis)

                        val isHighlighted = dayTimestamp == selectedDateTimestamp.value // 현재 선택/하이라이트된 날짜
                        val hasEvent = eventDates.contains(dayTimestamp) // 이벤트가 있는 날짜

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isHighlighted) Color(0xFF2563EB) else Color.Transparent) // 파란색 하이라이트
                                .clickable {
                                    selectedDateTimestamp.value = dayTimestamp
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayOfMonth.toString(),
                                color = if (isHighlighted) Color.White else if (hasEvent) Color(0xFF2563EB) else Color.Black, // 이벤트 있는 날짜는 파란색
                                fontWeight = if (hasEvent || isHighlighted) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 3. 선택된 날짜 이벤트 목록
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = if (selectedEvents.value.isNotEmpty()) "${formatTimestamp(selectedDateTimestamp.value)} 이벤트" else "선택된 날짜에 기록이 없습니다.",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(4.dp))

                        if (selectedEvents.value.isNotEmpty()) {
                            selectedEvents.value.forEach { event ->
                                Text(event.title, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onClose) { Text("확인") } }
    )
}