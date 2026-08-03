package com.example.schulte.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formats elapsed milliseconds as a readable time, e.g. "8.73秒" or "1分24.50秒". */
fun formatTime(elapsedMs: Long): String {
    val totalSeconds = elapsedMs / 1000.0
    return if (totalSeconds < 60.0) {
        String.format(Locale.CHINA, "%.2f秒", totalSeconds)
    } else {
        val minutes = (totalSeconds / 60.0).toInt()
        val seconds = totalSeconds - minutes * 60.0
        String.format(Locale.CHINA, "%d分%05.2f秒", minutes, seconds)
    }
}

/** Live timer display, e.g. "8.73". */
fun formatLive(elapsedMs: Long): String {
    return String.format(Locale.CHINA, "%.2f", elapsedMs / 1000.0)
}

/** Formats a unix timestamp as "yyyy-MM-dd HH:mm". */
fun formatTimestamp(timestampMs: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(timestampMs))
}