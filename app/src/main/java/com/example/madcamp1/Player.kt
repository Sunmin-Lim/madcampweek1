package com.example.madcamp1
import java.time.LocalDate
import java.time.LocalDateTime

data class Player(
    val name: String,
    val position: String,
    val number: Int,
    val photoResId: Int,
    val availableSlots: List<String>
)

