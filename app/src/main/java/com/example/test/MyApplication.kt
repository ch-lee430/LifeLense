package com.example.test

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Hilt 의존성 주입을 위한 애플리케이션 클래스
@HiltAndroidApp
class MainApplication : Application() {
    // 여기에 추가적인 전역 초기화 로직을 넣을 수 있습니다.
}