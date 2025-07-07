package com.example.madcamp1
import android.net.Uri
import java.time.LocalDate
import java.time.LocalDateTime

data class Player(
    val name: String,
    val position: String,
    val number: Int,
    val photoResId: Int,
    val availableSlots: List<String>,
    val uri: Uri? = null, // 갤러리/카메라로 선택된 이미지 URI
    val tag: List<String>   // 선수 특징 태그
)

