package com.example.schulte.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schulte.model.GameMode
import com.example.schulte.ui.theme.InkGreen
import com.example.schulte.ui.theme.InkGreenSoft
import com.example.schulte.ui.theme.WarmOrange
import com.example.schulte.ui.theme.WarmOrangeSoft
import com.example.schulte.util.formatTime

@Composable
fun HomeScreen(
    bestTime4x4: Long?,
    bestTime5x5: Long?,
    recordCount: Int,
    onModeSelected: (GameMode) -> Unit,
    onOpenHistory: () -> Unit,
) {
    AmbientBackground(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Header()

            Spacer(modifier = Modifier.height(32.dp))

            val best = minOfNotNull(bestTime4x4, bestTime5x5)
            if (best != null) {
                BestBadge(timeMs = best)
                Spacer(modifier = Modifier.height(30.dp))
            }

            SectionLabel(text = "训练模式")

            Spacer(modifier = Modifier.height(14.dp))

            // Bento: wide 4x4 card on top, 5x5 + history side by side below
            ModeCard(
                mode = GameMode.FOUR,
                bestTime = bestTime4x4,
                gradient = listOf(WarmOrangeSoft, WarmOrange),
                onClick = { onModeSelected(GameMode.FOUR) },
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ModeTile(
                    mode = GameMode.FIVE,
                    bestTime = bestTime5x5,
                    gradient = listOf(InkGreenSoft, InkGreen),
                    modifier = Modifier.weight(1.25f),
                    onClick = { onModeSelected(GameMode.FIVE) },
                )
                HistoryTile(
                    recordCount = recordCount,
                    modifier = Modifier.weight(0.75f),
                    onClick = onOpenHistory,
                )
            }

            Spacer(modifier = Modifier.height(44.dp))
        }
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(listOf(WarmOrangeSoft, WarmOrange))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "5",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "舒尔特方格",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "专注力 · 感知力 · 眼脑协同训练",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 6.dp),
    )
}

@Composable
private fun BestBadge(timeMs: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = "🏆", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "最佳成绩 ${formatTime(timeMs)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ModeCard(
    mode: GameMode,
    bestTime: Long?,
    gradient: List<Color>,
    onClick: () -> Unit,
) {
    val brush = Brush.linearGradient(gradient)
    FloatingCard(
        onClick = onClick,
        contentPadding = PaddingValues(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RaisedTile(
                cornerRadius = 24.dp,
                background = brush,
                modifier = Modifier.size(96.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(mode.size) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(mode.size) { col ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Color.White.copy(
                                                alpha = if ((row + col) % 2 == 0) 0.90f else 0.42f
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mode.subtitleLight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (bestTime != null) "最佳 ${formatTime(bestTime)}" else "还没有记录",
                    style = MaterialTheme.typography.labelLarge,
                    color = gradient.first(),
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.size(10.dp))
            ArrowPill(accent = brush)
        }
    }
}

@Composable
private fun ModeTile(
    mode: GameMode,
    bestTime: Long?,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val brush = Brush.linearGradient(gradient)
    FloatingCard(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(18.dp),
    ) {
        Column {
            RaisedTile(
                cornerRadius = 18.dp,
                background = brush,
                modifier = Modifier.size(64.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(9.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    repeat(mode.size) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(mode.size) { col ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Color.White.copy(
                                                alpha = if ((row + col) % 2 == 0) 0.92f else 0.46f
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (bestTime != null) "最佳 ${formatTime(bestTime)}" else "还没有记录",
                        style = MaterialTheme.typography.labelLarge,
                        color = gradient.first(),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
                ArrowPill(accent = brush, size = 32.dp)
            }
        }
    }
}

@Composable
private fun HistoryTile(
    recordCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FloatingCard(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(18.dp),
    ) {
        Column {
            RaisedTile(
                cornerRadius = 18.dp,
                background = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    )
                ),
                modifier = Modifier.size(64.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    ChartBar(fraction = 0.55f)
                    ChartBar(fraction = 0.78f)
                    ChartBar(fraction = 0.95f)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "历史记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (recordCount > 0) "共 $recordCount 条" else "暂无记录",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ChartBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(fraction)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
    )
}

private fun minOfNotNull(a: Long?, b: Long?): Long? = when {
    a != null && b != null -> minOf(a, b)
    a != null -> a
    b != null -> b
    else -> null
}
