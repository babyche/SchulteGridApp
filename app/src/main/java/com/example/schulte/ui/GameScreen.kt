package com.example.schulte.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schulte.SchulteViewModel
import com.example.schulte.model.CellState
import com.example.schulte.model.GameMode
import com.example.schulte.model.SchulteRecord
import com.example.schulte.util.formatLive
import com.example.schulte.util.formatTime
import com.example.schulte.util.SoundManager
import com.example.schulte.ui.theme.InkGreen
import com.example.schulte.ui.theme.WarmOrange
import com.example.schulte.ui.theme.WarmOrangeSoft
import kotlinx.coroutines.delay
import kotlin.random.Random

private enum class Phase { READY, COUNTDOWN, PLAYING, DONE }

private const val COUNTDOWN_START = 3

@Composable
fun GameScreen(
    mode: GameMode,
    viewModel: SchulteViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val sounds = remember { SoundManager(context) }
    DisposableEffect(Unit) {
        onDispose { sounds.release() }
    }

    var round by remember { mutableIntStateOf(0) }
    key(round) {
        GameRound(
            mode = mode,
            viewModel = viewModel,
            sounds = sounds,
            onBack = onBack,
            onRestart = { round += 1 },
        )
    }
}

@Composable
private fun GameRound(
    mode: GameMode,
    viewModel: SchulteViewModel,
    sounds: SoundManager,
    onBack: () -> Unit,
    onRestart: () -> Unit,
) {
    val cellCount = mode.count
    val best = remember(mode) { viewModel.bestTime(mode) }

    var phase by remember { mutableStateOf(Phase.READY) }
    var numbers by remember { mutableStateOf(emptyList<Int>()) }
    var solvedNumbers by remember { mutableStateOf(setOf<Int>()) }
    var target by remember { mutableIntStateOf(1) }
    var mistakes by remember { mutableIntStateOf(0) }
    var isNewRecord by remember { mutableStateOf(false) }
    var finishedElapsed by remember { mutableLongStateOf(0L) }

    var startTime by remember { mutableLongStateOf(0L) }
    var elapsed by remember { mutableLongStateOf(0L) }
    var countdownValue by remember { mutableIntStateOf(COUNTDOWN_START) }
    val running = phase == Phase.PLAYING && startTime != 0L

    LaunchedEffect(running) {
        if (running) {
            while (true) {
                elapsed = System.currentTimeMillis() - startTime
                delay(33)
            }
        }
    }

    var shakeTick by remember { mutableIntStateOf(0) }
    var wrongCell by remember { mutableIntStateOf(-1) }
    LaunchedEffect(shakeTick) {
        if (shakeTick > 0) {
            delay(450)
            wrongCell = -1
        }
    }

    // 3-second countdown, then the game starts and the timer begins immediately.
    LaunchedEffect(phase) {
        if (phase == Phase.COUNTDOWN) {
            for (i in COUNTDOWN_START downTo 0) {
                countdownValue = i
                delay(1000)
            }
            startTime = System.currentTimeMillis()
            numbers = (1..cellCount).toList().shuffled(Random(System.nanoTime()))
            phase = Phase.PLAYING
        }
    }

    fun startTraining() {
        numbers = emptyList()
        solvedNumbers = emptySet()
        target = 1
        mistakes = 0
        isNewRecord = false
        finishedElapsed = 0L
        startTime = 0L
        elapsed = 0L
        shakeTick = 0
        wrongCell = -1
        countdownValue = COUNTDOWN_START
        phase = Phase.COUNTDOWN
    }

    fun onCellTapped(index: Int) {
        if (phase != Phase.PLAYING) return
        val tappedNumber = numbers[index]
        if (tappedNumber == target) {
            sounds.playCorrect()
            solvedNumbers = solvedNumbers + target
            if (target == cellCount) {
                finishedElapsed = System.currentTimeMillis() - startTime
                elapsed = finishedElapsed
                phase = Phase.DONE
                isNewRecord = viewModel.submitResult(mode, finishedElapsed)
                viewModel.addRecord(
                    SchulteRecord(
                        mode = mode,
                        elapsedMs = finishedElapsed,
                        mistakes = mistakes,
                        timestamp = System.currentTimeMillis(),
                    )
                )
            } else {
                target += 1
            }
        } else {
            sounds.playWrong()
            mistakes += 1
            wrongCell = index
            shakeTick += 1
        }
    }

    val finished = phase == Phase.DONE
    val progress = if (finished) 1f else (solvedNumbers.size.toFloat() / cellCount)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AmbientBackground(modifier = Modifier.fillMaxSize()) {}
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mode.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (best != null) "最佳 ${formatTime(best)}" else "从 1 开始，依次点击",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (mistakes == 0) {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "误 $mistakes",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (mistakes == 0) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (phase == Phase.READY) {
                // ---- Ready state: numbers hidden until the countdown finishes ----
                ReadySection(
                    mode = mode,
                    onStart = { startTraining() },
                )
            } else {
                // Timer + progress
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatLive(elapsed),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = " 秒",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Next-number prompt, centered and clearly separated from the grid
                NextNumberBadge(
                    number = target,
                    finished = finished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 60.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid (pushed down so it never overlaps the prompt). Only shown once
                // the countdown is over and the numbers have been generated.
                if (numbers.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        repeat(mode.size) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                repeat(mode.size) { col ->
                                    val index = row * mode.size + col
                                    val number = numbers[index]
                                    val cell = CellState(
                                        number = number,
                                        isSolved = number in solvedNumbers,
                                    )
                                    NumberCell(
                                        cell = cell,
                                        isWrong = index == wrongCell,
                                        shakeKey = shakeTick,
                                        fontSize = if (mode.size == 4) 26.sp else 20.sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp),
                                        onClick = { onCellTapped(index) },
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Countdown overlay (covers the screen, hides the upcoming grid)
        AnimatedVisibility(visible = phase == Phase.COUNTDOWN, modifier = Modifier.align(Alignment.Center)) {
            CountdownOverlay(countdownValue = countdownValue)
        }

        // Result overlay — full-screen container that vertically & horizontally centers
        // the completion card over a soft scrim for a clean, focused finish.
        AnimatedVisibility(visible = finished, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                ResultCard(
                    mode = mode,
                    elapsedMs = finishedElapsed,
                    mistakes = mistakes,
                    isNewRecord = isNewRecord,
                    onRestart = onRestart,
                    onHome = onBack,
                )
            }
        }
    }
}

@Composable
private fun ReadySection(mode: GameMode, onStart: () -> Unit) {
    FloatingCard(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 34.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 48.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(WarmOrangeSoft, WarmOrange))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🧠", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "准备开始 · ${mode.title}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击「开始训练」后先进行 3 秒倒计时\n数字会在开始时随机生成，避免提前记忆",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(30.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(listOf(WarmOrangeSoft, WarmOrange))
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStart,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "开始训练",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CountdownOverlay(countdownValue: Int) {
    val scale = remember { Animatable(0.5f) }
    LaunchedEffect(countdownValue) {
        scale.snapTo(0.5f)
        scale.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 600f))
    }
    val numberBrush = Brush.verticalGradient(listOf(WarmOrangeSoft, WarmOrange))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.32f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (countdownValue > 0) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$countdownValue",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge.copy(
                            brush = numberBrush,
                            fontSize = 96.sp,
                        ),
                        modifier = Modifier.graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        },
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
            if (countdownValue == 0) {
                Text(
                    text = "开始！",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge.copy(
                        brush = numberBrush,
                        fontSize = 72.sp,
                    ),
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
                )
            } else {
                Text(
                    text = "准备开始",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun NumberCell(
    cell: CellState,
    isWrong: Boolean,
    shakeKey: Int,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(shakeKey) {
        // Always snap back to center first: when the effect is restarted before a
        // previous shake finished (e.g. a second wrong tap), this guarantees the
        // cell never stays offset after the shake.
        offsetX.snapTo(0f)
        if (isWrong) {
            listOf(-10f, 8f, -5f, 0f).forEach { target ->
                offsetX.animateTo(target, tween(durationMillis = 90, easing = LinearEasing))
            }
        }
    }

    val shape = RoundedCornerShape(18.dp)
    val background = if (isWrong) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val textColor = if (isWrong) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .offset(x = offsetX.value.dp)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.10f),
            )
            .clip(shape)
            .background(background)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                    startY = 0f,
                    endY = 0.45f,
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.6f), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cell.number.toString(),
            fontSize = fontSize,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NextNumberBadge(
    number: Int,
    finished: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(50), ambientColor = WarmOrange.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(listOf(WarmOrangeSoft, WarmOrange))
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (finished) "完成" else "下一个",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f),
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = if (finished) "✔" else number.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun ResultCard(
    mode: GameMode,
    elapsedMs: Long,
    mistakes: Int,
    isNewRecord: Boolean,
    onRestart: () -> Unit,
    onHome: () -> Unit,
) {
    FloatingCard(
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (isNewRecord) "🎉 新纪录！" else "✅ 完成！",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isNewRecord) InkGreen
                        else MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${mode.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ResultStat(label = "用时", value = formatTime(elapsedMs))
                ResultStat(label = "错误", value = "$mistakes 次")
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onHome,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("返回首页", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(listOf(WarmOrangeSoft, WarmOrange))
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onRestart,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("再来一局", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ResultStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}