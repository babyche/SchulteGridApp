package com.example.schulte.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.schulte.model.GameMode
import com.example.schulte.ui.theme.Indigo
import com.example.schulte.ui.theme.IndigoLight
import com.example.schulte.ui.theme.Mint
import com.example.schulte.util.formatTime

@Composable
fun HomeScreen(
    bestTime4x4: Long?,
    bestTime5x5: Long?,
    recordCount: Int,
    onModeSelected: (GameMode) -> Unit,
    onOpenHistory: () -> Unit,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.background,
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(listOf(Indigo, IndigoLight))
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
                    )
                    Text(
                        text = "专注力 · 感知力 · 眼脑协同训练",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Best overall
            val best = minOfNotNull(bestTime4x4, bestTime5x5)
            if (best != null) {
                BestBadge(timeMs = best)
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Mode cards
            Text(
                text = "选择训练模式",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            ModeCard(
                mode = GameMode.FOUR,
                bestTime = bestTime4x4,
                accent = Indigo,
                onClick = { onModeSelected(GameMode.FOUR) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            ModeCard(
                mode = GameMode.FIVE,
                bestTime = bestTime5x5,
                accent = Mint,
                onClick = { onModeSelected(GameMode.FIVE) },
            )

            Spacer(modifier = Modifier.height(16.dp))
            HistoryCard(recordCount = recordCount, onClick = onOpenHistory)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun HistoryCard(recordCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Bar-chart preview (like the grid preview in ModeCard)
            Column(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChartBar(fraction = 0.5f)
                ChartBar(fraction = 0.72f)
                ChartBar(fraction = 0.92f)
            }

            Spacer(modifier = Modifier.size(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "每次训练成绩自动保存",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (recordCount > 0) "共 $recordCount 条记录" else "还没有记录",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "→",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ChartBar(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(fraction)
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
    )
}

@Composable
private fun ModeCard(
    mode: GameMode,
    bestTime: Long?,
    accent: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = accent.copy(alpha = 0.25f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Grid preview
            Column(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(mode.size) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(mode.size) { col ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if ((row + col) % 2 == 0) accent.copy(alpha = 0.55f)
                                        else accent.copy(alpha = 0.30f)
                                    )
                            )
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
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mode.subtitleLight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (bestTime != null) "最佳 ${formatTime(bestTime)}" else "还没有记录",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "→",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun BestBadge(timeMs: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp),
            )
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "🏆",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "最佳成绩 ${formatTime(timeMs)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

private fun minOfNotNull(a: Long?, b: Long?): Long? = when {
    a != null && b != null -> minOf(a, b)
    a != null -> a
    b != null -> b
    else -> null
}