package com.example.madcamp1
import android.net.Uri
import java.time.LocalDate
import java.time.LocalDateTime

data class Player(
    val name: String,
    val position: String,
    val number: Int,
    val availableSlots: List<String>,
    val photoResId: Int,
    val uri: Uri? = null
)