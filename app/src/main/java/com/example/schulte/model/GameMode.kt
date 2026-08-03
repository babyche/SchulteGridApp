package com.example.schulte.model

enum class GameMode(
    val size: Int,
    val count: Int,
    val title: String,
    val subtitleLight: String,
) {
    FOUR(
        size = 4,
        count = 16,
        title = "4 × 4 数字网格",
        subtitleLight = "16 个数字 · 快速热身",
    ),
    FIVE(
        size = 5,
        count = 25,
        title = "5 × 5 数字网格",
        subtitleLight = "25 个数字 · 进阶挑战",
    );
}

data class CellState(
    val number: Int,
    val isSolved: Boolean,
) : java.io.Serializable