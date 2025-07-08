package com.example.madcamp1

import android.net.Uri

data class Player(
    val name: String,
    val position: String,
    val number: Int,
    val availableSlots: List<String>,
    val uri: Uri? = null, // 갤러리/카메라로 선택된 이미지 URI
    val tag: List<String>,   // 선수 특징 태그
    val photoResId: Int,

    // 위치 정보
    val latitude: Double? = null,
    val longitude: Double? = null
)