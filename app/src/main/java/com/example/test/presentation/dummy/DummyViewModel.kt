package com.example.test.presentation.dummy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.domain.model.Memory
import com.example.test.domain.usecase.DummyMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val isPlaceholder: Boolean = false // 로딩 메시지 식별용
)

enum class Sender {
    USER, GEMINI
}

// UI 상태 정의 (효율적인 상태 관리)
sealed class UiState {
    object Ready : UiState()
    object Analyzing : UiState() // 로딩 중
    data class Result(val memory: Memory) : UiState()
    data class Error(val message: String) : UiState()
}


@HiltViewModel
class DummyViewModel @Inject constructor(
    val dummyMemoryUseCase: DummyMemoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Ready)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _questionText = MutableStateFlow("")
    val questionText: StateFlow<String> = _questionText.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val LOADING_MESSAGE_ID = "LOADING_GEMINI_ANALYSIS"

    init {
        // 앱 시작 시 초기 Gemini 메시지 전송
        val initialMessage = Message(
            sender = Sender.GEMINI,
            text = "🤖 과거 기억을 찾아드릴게요.\n어떤 기억을 떠올리고 싶나요?"
        )
        _messages.value = listOf(initialMessage)
    }

    fun updateQuestionText(newText: String) {
        _questionText.value = newText
    }

    fun findPastMemory() {
        val userQuestion = _questionText.value.trim()
        if (userQuestion.isBlank() || _uiState.value is UiState.Analyzing) return

        // 1. 사용자 메시지를 채팅 리스트에 추가
        val userMessage = Message(sender = Sender.USER, text = userQuestion)
        _messages.value = _messages.value + userMessage
        _questionText.value = "" // 입력창 비우기

        // 2. Gemini 로딩 상태 메시지 추가 및 상태 변경
        val loadingMessage = Message(
            id = LOADING_MESSAGE_ID,
            sender = Sender.GEMINI,
            text = "과거기억을 살펴보는 중입니다...",
            isPlaceholder = true
        )
        _messages.value = _messages.value + loadingMessage
        _uiState.value = UiState.Analyzing

        viewModelScope.launch {
            try {
                // 3. Dummy Use Case 호출 (더미 데이터 반환)
                val memory: Memory = dummyMemoryUseCase(userQuestion)

                // 4. 로딩 메시지 제거
                removePlaceholderMessage()

                // 5. Gemini의 최종 답변 메시지를 리스트에 추가
                val geminiReply = Message(sender = Sender.GEMINI, text = memory.content)
                _messages.value = _messages.value + geminiReply

                // 6. 상태를 성공(Result)으로 업데이트 (이 상태가 유지되어 ReferenceCard가 표시됨)
                _uiState.value = UiState.Result(memory)
            } catch (e: Exception) {
                removePlaceholderMessage()
                val errorMessage = "오류가 발생했습니다: ${e.message ?: "알 수 없는 오류"}"
                _messages.value = _messages.value + Message(sender = Sender.GEMINI, text = errorMessage)
                _uiState.value = UiState.Error(errorMessage)
            } finally {
                // [수정된 부분]: Result 상태를 유지하여 레퍼런스 카드가 표시되도록 함.
                // 다음 질문이 들어올 때까지 Ready로 전환하지 않음.
            }
        }
    }

    private fun removePlaceholderMessage() {
        _messages.value = _messages.value.filter { it.id != LOADING_MESSAGE_ID }
    }
}