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

// UI 모델 정의

@HiltViewModel
class CommunicationViewModel @Inject constructor(
    private val getPastMemoryUseCase: GetPastMemoryUseCase
) : ViewModel() {

}