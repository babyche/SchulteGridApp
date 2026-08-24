package com.example.schulte.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schulte.SchulteViewModel
import com.example.schulte.model.GameMode
import kotlin.math.roundToLong

private val RECENT_OPTIONS = listOf(10, 30, 60)

@Composable
fun RecentDetailScreen(
    viewModel: SchulteViewModel,
    onBack: () -> Unit,
) {
    var count by remember { mutableIntStateOf(RECENT_OPTIONS.first()) }
    val fourSlots = remember(count) { viewModel.recentSessions(GameMode.FOUR, count) }
    val fiveSlots = remember(count) { viewModel.recentSessions(GameMode.FIVE, count) }
    val fourAvg = fourSlots.mapNotNull { it.fourAvgMs }.takeIf { it.isNotEmpty() }?.average()?.roundToLong()
    val fiveAvg = fiveSlots.mapNotNull { it.fiveAvgMs }.takeIf { it.isNotEmpty() }?.average()?.roundToLong()

    AmbientBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.size(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "返回",
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "最近用时",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.size(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RECENT_OPTIONS.forEach { option ->
                    SoftChip(
                        label = "最近 $option 次",
                        selected = count == option,
                        onClick = { count = option },
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "横轴为第几次训练，点数较多时可缩放并左右滑动查看",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp),
                )
                TrendCard(
                    title = "4 × 4 最近用时",
                    subtitle = "最近 $count 次 · 用时（秒），虚线为平均值",
                    slots = fourSlots,
                    averageMs = fourAvg,
                    tooltipShowLabel = false,
                )
                TrendCard(
                    title = "5 × 5 最近用时",
                    subtitle = "最近 $count 次 · 用时（秒），虚线为平均值",
                    slots = fiveSlots,
                    averageMs = fiveAvg,
                    tooltipShowLabel = false,
                )
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}
