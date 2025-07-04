package com.example.madcamp1

import android.net.Uri

data class Photo(
    val resourceId: Int = 0,             // For bundled app drawables
    val uri: Uri? = null,                // For user-picked images
    val date: String = "",               // Optional date info
    val description: String = ""         // Optional description
)
