package com.example.schulte.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schulte.SchulteViewModel
import com.example.schulte.model.GameMode
import com.example.schulte.model.SchulteRecord
import com.example.schulte.util.formatTime
import com.example.schulte.util.formatTimestamp

private enum class ModeFilter(val label: String) {
    ALL("全部"),
    FOUR("4 × 4"),
    FIVE("5 × 5"),
}

private enum class SortOrder(val label: String) {
    RECENT("最近"),
    FASTEST("最快"),
}

@Composable
fun HistoryScreen(
    viewModel: SchulteViewModel,
    onBack: () -> Unit,
) {
    var modeFilter by remember { mutableStateOf(ModeFilter.ALL) }
    var sortOrder by remember { mutableStateOf(SortOrder.RECENT) }
    var refreshKey by remember { mutableIntStateOf(0) }

    val allRecords = remember(refreshKey) { viewModel.loadRecords() }
    val filtered = allRecords.filter { r ->
        when (modeFilter) {
            ModeFilter.ALL -> true
            ModeFilter.FOUR -> r.mode == GameMode.FOUR
            ModeFilter.FIVE -> r.mode == GameMode.FIVE
        }
    }.let { list ->
        when (sortOrder) {
            SortOrder.RECENT -> list.sortedByDescending { it.timestamp }
            SortOrder.FASTEST -> list.sortedBy { it.elapsedMs }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "返回",
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    viewModel.clearRecords()
                    refreshKey += 1
                }) {
                    Text(
                        text = "清空",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filters: single row, horizontally scrollable so it always fits the screen width
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModeFilter.entries.forEach { f ->
                    FilterChip(
                        selected = modeFilter == f,
                        onClick = { modeFilter = f },
                        label = { Text(f.label, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                SortOrder.entries.forEach { s ->
                    FilterChip(
                        selected = sortOrder == s,
                        onClick = { sortOrder = s },
                        label = { Text(s.label, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filtered.isEmpty()) {
                EmptyState(
                    hasAny = allRecords.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.timestamp }) { record ->
                        RecordRow(
                            record = record,
                            rank = rankOf(record, filtered),
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

private fun rankOf(record: SchulteRecord, sortedList: List<SchulteRecord>): Int {
    // Same-mode fastest rank among the currently displayed list
    val sameMode = sortedList.filter { it.mode == record.mode }
    val idx = sameMode.indexOfFirst { it.timestamp == record.timestamp }
    return if (idx >= 0) idx + 1 else 0
}

@Composable
private fun RecordRow(record: SchulteRecord, rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Mode badge
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (record.mode == GameMode.FOUR) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (record.mode == GameMode.FOUR) "4" else "5",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (record.mode == GameMode.FOUR) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
            )
        }

        Spacer(modifier = Modifier.size(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatTime(record.elapsedMs),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (rank == 1) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "🏅",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Text(
                text = "错误 ${record.mistakes} 次 · ${formatTimestamp(record.timestamp)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(hasAny: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "📝", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasAny) "没有符合条件的记录" else "还没有训练记录",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "完成一局舒尔特方格后会自动保存在这里",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}