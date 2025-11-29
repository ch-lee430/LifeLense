package com.example.test.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.domain.model.Memory
import com.example.test.domain.model.CalendarEvent
import com.example.test.domain.usecase.GetPastMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val isPlaceholder: Boolean = false
)

enum class Sender {
    USER, GEMINI
}

sealed class UiState {
    object Ready : UiState()
    object Analyzing : UiState()
    data class Result(val memory: Memory) : UiState()
    data class Error(val message: String) : UiState()
}

@HiltViewModel
class CommunicationViewModel @Inject constructor(
    private val getPastMemoryUseCase: GetPastMemoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Ready)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _questionText = MutableStateFlow("")
    val questionText: StateFlow<String> = _questionText.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val LOADING_MESSAGE_ID = "LOADING_GEMINI_ANALYSIS"

    init {
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

        val userMessage = Message(sender = Sender.USER, text = userQuestion)
        _messages.value = _messages.value + userMessage
        _questionText.value = ""

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
                val memory: Memory = getPastMemoryUseCase(userQuestion)

                removePlaceholderMessage()

                val geminiReply = Message(sender = Sender.GEMINI, text = memory.content)
                _messages.value = _messages.value + geminiReply

                _uiState.value = UiState.Result(memory)
            } catch (e: Exception) {
                removePlaceholderMessage()
                val errorMessage = "기록 분석 중 오류가 발생했습니다: ${e.message ?: "알 수 없는 오류"}"
                _messages.value = _messages.value + Message(sender = Sender.GEMINI, text = errorMessage)
                _uiState.value = UiState.Error(errorMessage)
            }
        }
    }

    private fun removePlaceholderMessage() {
        _messages.value = _messages.value.filter { it.id != LOADING_MESSAGE_ID }
    }

    /**
     * 캘린더 Dialog용 이벤트 목록을 Use Case를 통해 가져오는 함수.
     */
    suspend fun getCalendarEventsForDialog(timestamp: Long): List<CalendarEvent> {
        // GetPastMemoryUseCase의 함수를 호출
        return getPastMemoryUseCase.getCalendarEventsForDialog(timestamp)
    }
}