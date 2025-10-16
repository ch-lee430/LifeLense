package com.example.test.presentation.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.domain.model.Memory
import com.example.test.domain.usecase.GetPastMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

// 채팅 메시지를 위한 데이터 클래스
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
)

enum class Sender {
    USER, GEMINI
}

@HiltViewModel
class CommunicationViewModel @Inject constructor(
    private val getPastMemoryUseCase: GetPastMemoryUseCase
) : ViewModel() {

    // UI 상태 관리를 위한 StateFlow
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 사용자 질문 텍스트 상태
    private val _questionText = MutableStateFlow("")
    val questionText: StateFlow<String> = _questionText.asStateFlow()

    // 채팅 메시지 리스트 상태
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun updateQuestionText(newText: String) {
        _questionText.value = newText
    }

    fun findPastMemory() {
        val userQuestion = _questionText.value.trim()
        if (userQuestion.isBlank()) return

        // 1. 사용자 메시지를 채팅 리스트에 추가
        _messages.value = _messages.value + Message(sender = Sender.USER, text = userQuestion)
        _questionText.value = "" // 입력창 비우기

        // 2. 비동기 작업 시작 및 로딩 상태 변경
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                // 3. Domain Layer의 Use Case 호출
                val memory = getPastMemoryUseCase(userQuestion)

                // 4. Gemini의 답변을 메시지 리스트에 추가
                _messages.value = _messages.value + Message(sender = Sender.GEMINI, text = memory.content)

                // 5. 작업 완료 후 성공 상태로 UI 업데이트
                _uiState.value = UiState.Success(memory)
            } catch (e: Exception) {
                // 6. 오류 발생 시 에러 메시지 메시지 리스트에 추가
                val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                _messages.value = _messages.value + Message(sender = Sender.GEMINI, text = "오류: $errorMessage")
                _uiState.value = UiState.Error(errorMessage)
            } finally {
                // `finally` 블록에서 `uiState`를 다시 `Initial`로 변경하여
                // 다음 질문을 받을 준비를 하거나, 별도 상태 관리를 할 수 있습니다.
            }
        }
    }
}

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    data class Success(val memory: Memory) : UiState()
    data class Error(val message: String) : UiState()
}