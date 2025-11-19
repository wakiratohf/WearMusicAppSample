package com.toh.shared.model

data class AudioItem(
    val id: Long,
    val title: String,
    val fileName: String,
    val duration: Long,
    val size: Long,
    val uri: String
)