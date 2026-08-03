package com.example.schulte.model

data class SchulteRecord(
    val mode: GameMode,
    val elapsedMs: Long,
    val mistakes: Int,
    val timestamp: Long,
)