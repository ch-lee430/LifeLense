package com.example.test.presentation.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InputRow(
    questionText: String,
    onQuestionTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .navigationBarsPadding(),
        // ★★★ 수정: imePadding() 및 navigationBarsPadding() 모두 제거 ★★★
        // Scaffold의 bottomBar가 알아서 하단 시스템 바를 피하고, LazyColumn의 contentPadding이 InputRow 높이를 고려하게 함
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = questionText,
            onValueChange = onQuestionTextChange,
            placeholder = { Text("과거에 대해 질문하세요") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSendClick,
            enabled = !isLoading && questionText.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Filled.Send, contentDescription = "기억 찾기")
        }
    }
}