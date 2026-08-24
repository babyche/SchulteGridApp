package com.example.schulte.model

/**
 * A single bucket on the trend chart's x-axis (one day or one month).
 * Holds the average elapsed time per mode; `null` means no records in that bucket.
 */
data class TrendSlot(
    val label: String,
    val fourAvgMs: Long?,
    val fiveAvgMs: Long?,
)
