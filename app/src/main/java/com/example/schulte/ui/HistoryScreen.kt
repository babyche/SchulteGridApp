package com.example.schulte.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.schulte.SchulteViewModel
import com.example.schulte.model.GameMode
import com.example.schulte.model.SchulteRecord
import com.example.schulte.ui.theme.InkGreen
import com.example.schulte.ui.theme.InkGreenSoft
import com.example.schulte.ui.theme.WarmOrange
import com.example.schulte.ui.theme.WarmOrangeSoft
import com.example.schulte.util.formatTime
import com.example.schulte.util.formatTimestamp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

    AmbientBackground(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

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
                    color = MaterialTheme.colorScheme.onSurface,
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

            Spacer(modifier = Modifier.height(18.dp))

            // Filters: single horizontally-scrollable row of soft chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModeFilter.entries.forEach { f ->
                    SoftChip(
                        label = f.label,
                        selected = modeFilter == f,
                        onClick = { modeFilter = f },
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                SortOrder.entries.forEach { s ->
                    SoftChip(
                        label = s.label,
                        selected = sortOrder == s,
                        onClick = { sortOrder = s },
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filtered, key = { it.timestamp }) { record ->
                        SwipeToDeleteRow(
                            onDelete = {
                                viewModel.deleteRecord(record.timestamp)
                                refreshKey += 1
                            },
                        ) {
                            RecordRow(
                                record = record,
                                rank = rankOf(record, filtered),
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

private fun rankOf(record: SchulteRecord, sortedList: List<SchulteRecord>): Int {
    val sameMode = sortedList.filter { it.mode == record.mode }
    val idx = sameMode.indexOfFirst { it.timestamp == record.timestamp }
    return if (idx >= 0) idx + 1 else 0
}

@Composable
private fun SwipeToDeleteRow(
    revealWidth: Dp = 84.dp,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val revealWidthPx = with(density) { revealWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var swiped by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (swiped) {
            Box(
                modifier = Modifier
                    .matchParentSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(revealWidth)
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onDelete,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "删除",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { scope.launch { offsetX.stop() } },
                        onHorizontalDrag = { _, dragAmount ->
                            val newValue = (offsetX.value + dragAmount).coerceIn(-revealWidthPx, 0f)
                            scope.launch { offsetX.snapTo(newValue) }
                            swiped = newValue < -revealWidthPx * 0.35f
                        },
                        onDragEnd = {
                            val target = if (offsetX.value < -revealWidthPx * 0.5f) -revealWidthPx else 0f
                            scope.launch {
                                offsetX.animateTo(target, spring())
                                swiped = target < 0f
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, spring())
                                swiped = false
                            }
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun RecordRow(record: SchulteRecord, rank: Int) {
    val accent = if (record.mode == GameMode.FOUR) {
        listOf(WarmOrangeSoft, WarmOrange)
    } else {
        listOf(InkGreenSoft, InkGreen)
    }
    val gradient = Brush.linearGradient(accent)

    FloatingCard(
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        tint = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(15.dp),
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.12f),
                    )
                    .clip(RoundedCornerShape(15.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (record.mode == GameMode.FOUR) "4" else "5",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = formatTime(record.elapsedMs),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (rank == 1) {
                        Text(
                            text = "🏅",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "错误 ${record.mistakes} 次 · ${formatTimestamp(record.timestamp)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "📝",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasAny) "没有符合条件的记录" else "还没有训练记录",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "完成一局舒尔特方格后会自动保存在这里",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}