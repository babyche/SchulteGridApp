package com.example.schulte.model

/**
 * A single bucket on the trend chart's x-axis (one day or one month).
 * Holds the aggregated elapsed time per mode (average or best depending on the screen);
 * `null` means no records in that bucket.
 */
data class TrendSlot(
    val label: String,
    val fourMs: Long?,
    val fiveMs: Long?,
)
