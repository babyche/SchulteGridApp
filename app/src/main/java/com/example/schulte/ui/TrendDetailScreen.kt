package com.example.schulte.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.schulte.SchulteViewModel

@Composable
fun TrendDetailScreen(
    viewModel: SchulteViewModel,
    onBack: () -> Unit,
) {
    val dailyTrend = remember { viewModel.dailyTrend() }
    val monthlyTrend = remember { viewModel.monthlyTrend() }

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
                    text = "训练趋势",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.size(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TrendCard(
                    title = "日平均耗时",
                    subtitle = "近 30 个训练日 · 每日平均用时（秒）",
                    slots = dailyTrend,
                )
                TrendCard(
                    title = "月平均耗时",
                    subtitle = "近 12 个月 · 每月平均用时（秒）",
                    slots = monthlyTrend,
                )
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}
