package com.example.schulte.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.schulte.model.TrendSlot
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round

private const val MAX_ZOOM = 8f

/**
 * Floating card showing an average-time trend chart, split into 4×4 (primary)
 * and 5×5 (secondary) lines.
 *
 * @param averageMs when set, draws a dashed horizontal line at this average time.
 * @param tooltipShowLabel whether the point tooltip includes the slot label.
 */
@Composable
fun TrendCard(
    title: String,
    subtitle: String,
    slots: List<TrendSlot>,
    modifier: Modifier = Modifier,
    averageMs: Long? = null,
    tooltipShowLabel: Boolean = true,
) {
    FloatingCard(
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (slots.none { it.fourAvgMs != null || it.fiveAvgMs != null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无数据，完成训练后自动生成",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                TrendChart(
                    slots = slots,
                    averageMs = averageMs,
                    tooltipShowLabel = tooltipShowLabel,
                )
                Spacer(modifier = Modifier.height(12.dp))
                ChartLegend(
                    hasFour = slots.any { it.fourAvgMs != null },
                    hasFive = slots.any { it.fiveAvgMs != null },
                )
            }
        }
    }
}

@Composable
private fun ChartLegend(hasFour: Boolean, hasFive: Boolean) {
    if (!hasFour && !hasFive) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        if (hasFour) {
            LegendItem(color = MaterialTheme.colorScheme.primary, label = "4 × 4")
        }
        if (hasFour && hasFive) {
            Spacer(modifier = Modifier.size(18.dp))
        }
        if (hasFive) {
            LegendItem(color = MaterialTheme.colorScheme.secondary, label = "5 × 5")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A single tappable point on the chart. */
private data class SeriesPoint(
    val series: Int,
    val index: Int,
    val pos: Offset,
    val valueMs: Long,
)

/** Plot geometry shared by drawing and hit-testing. */
private class ChartGeometry(
    val left: Float,
    val top: Float,
    val plotW: Float,
    val plotH: Float,
    val yMin: Float,
    val yMax: Float,
    val n: Int,
) {
    val baselineY: Float get() = top + plotH

    fun yPos(v: Float): Float = baselineY - (v - yMin) / (yMax - yMin) * plotH

    fun xPos(i: Int): Float = if (n <= 1) left + plotW / 2f else left + plotW * i / (n - 1f)
}

private fun buildGeometry(slots: List<TrendSlot>, width: Float, height: Float, density: Density): ChartGeometry {
    val left = with(density) { 52.dp.toPx() }
    val right = with(density) { 8.dp.toPx() }
    val top = with(density) { 6.dp.toPx() }
    val bottom = with(density) { 24.dp.toPx() }
    val plotW = width - left - right
    val plotH = height - top - bottom

    val values = slots.flatMap { listOfNotNull(it.fourAvgMs, it.fiveAvgMs) }
    var yMin = values.min().toFloat()
    var yMax = values.max().toFloat()
    if (yMin == yMax) {
        yMin -= 1f
        yMax += 1f
    }
    val pad = (yMax - yMin) * 0.15f
    yMin -= pad
    yMax += pad

    return ChartGeometry(left, top, plotW, plotH, yMin, yMax, slots.size)
}

private fun buildPoints(geo: ChartGeometry, slots: List<TrendSlot>, zoom: Float, panX: Float): List<SeriesPoint> {
    val points = mutableListOf<SeriesPoint>()
    slots.forEachIndexed { i, slot ->
        val x = geo.left + (geo.xPos(i) - geo.left) * zoom + panX
        if (slot.fourAvgMs != null) {
            points.add(SeriesPoint(0, i, Offset(x, geo.yPos(slot.fourAvgMs.toFloat())), slot.fourAvgMs))
        }
        if (slot.fiveAvgMs != null) {
            points.add(SeriesPoint(1, i, Offset(x, geo.yPos(slot.fiveAvgMs.toFloat())), slot.fiveAvgMs))
        }
    }
    return points
}

/**
 * Canvas-based line chart. Pinch to zoom (default 1x, up to [MAX_ZOOM]); once the
 * chart overflows the viewport it can be panned horizontally. Points are tappable.
 */
@Composable
fun TrendChart(
    slots: List<TrendSlot>,
    modifier: Modifier = Modifier,
    averageMs: Long? = null,
    tooltipShowLabel: Boolean = true,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val tooltipStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.inverseOnSurface,
    )
    val avgStyle = TextStyle(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    val axisLine = MaterialTheme.colorScheme.outlineVariant
    val avgColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val bubbleColor = MaterialTheme.colorScheme.inverseSurface
    val fourColor = MaterialTheme.colorScheme.primary
    val fiveColor = MaterialTheme.colorScheme.secondary

    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(slots) {
        zoom = 1f
        panX = 0f
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(slots) {
                val density = this
                val touchSlop = with(density) { 8.dp.toPx() }
                val tapSlop = with(density) { 12.dp.toPx() }
                val rightPx = with(density) { 8.dp.toPx() }
                val hitThreshold = with(density) { 28.dp.toPx() }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var maxPointers = 1
                    var pinching = false
                    var panning = false
                    var startZoom = zoom
                    var startPan = panX
                    var lastDist = 0f
                    var lastCentroid = down.position
                    var downPos = down.position
                    var lastPos = down.position

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        if (pressed.isEmpty()) break
                        maxPointers = maxOf(maxPointers, pressed.size)

                        if (pressed.size >= 2) {
                            // Pinch to zoom
                            val p0 = pressed[0].position
                            val p1 = pressed[1].position
                            val dist = (p1 - p0).getDistance()
                            val centroid = (p0 + p1) / 2f
                            if (!pinching) {
                                pinching = true
                                startZoom = zoom
                                startPan = panX
                                lastDist = dist
                                lastCentroid = centroid
                            }
                            val newZoom = if (lastDist == 0f) startZoom else (startZoom * (dist / lastDist)).coerceIn(1f, MAX_ZOOM)
                            val geo = buildGeometry(slots, size.width.toFloat(), size.height.toFloat(), density)
                            val viewportPlotW = size.width - geo.left - rightPx
                            val maxPan = maxOf(0f, geo.plotW * newZoom - viewportPlotW)
                            val naturalCentroid = (lastCentroid.x - geo.left - startPan) / startZoom
                            panX = (lastCentroid.x - geo.left - naturalCentroid * newZoom).coerceIn(-maxPan, 0f)
                            zoom = newZoom
                            lastDist = dist
                            lastCentroid = centroid
                            pressed.forEach { it.consume() }
                        } else {
                            val change = pressed[0]
                            lastPos = change.position
                            if (pinching) {
                                pinching = false
                                startZoom = zoom
                                startPan = panX
                                downPos = change.position
                            }
                            if (change.isConsumed) break
                            if (!panning) {
                                val total = change.position - downPos
                                if (abs(total.x) > touchSlop || abs(total.y) > touchSlop) {
                                    panning = abs(total.x) > abs(total.y)
                                    if (panning) startPan = panX
                                }
                            }
                            if (panning) {
                                // Horizontal pan only; vertical drags are left for the page scroll
                                val deltaX = change.position.x - change.previousPosition.x
                                val geo = buildGeometry(slots, size.width.toFloat(), size.height.toFloat(), density)
                                val viewportPlotW = size.width - geo.left - rightPx
                                val maxPan = maxOf(0f, geo.plotW * zoom - viewportPlotW)
                                panX = (panX + deltaX).coerceIn(-maxPan, 0f)
                                change.consume()
                            }
                        }
                    }

                    // Tap to select a point
                    if (maxPointers == 1 && !panning && !pinching && (lastPos - downPos).getDistance() < tapSlop) {
                        val geo = buildGeometry(slots, size.width.toFloat(), size.height.toFloat(), density)
                        val hit = buildPoints(geo, slots, zoom, panX).minByOrNull { (it.pos - lastPos).getDistance() }
                        selected = if (hit != null && (hit.pos - lastPos).getDistance() <= hitThreshold) {
                            hit.series to hit.index
                        } else {
                            null
                        }
                    }
                }
            },
    ) {
        val geo = buildGeometry(slots, this.size.width, this.size.height, this)
        val values = slots.flatMap { listOfNotNull(it.fourAvgMs, it.fiveAvgMs) }
        if (values.isEmpty()) return@Canvas

        fun xMapped(i: Int): Float = geo.left + (geo.xPos(i) - geo.left) * zoom + panX

        val plotStart = geo.left + panX
        val plotEnd = geo.left + geo.plotW * zoom + panX

        fun drawAxisLabel(text: String, x: Float, y: Float) {
            val layout = textMeasurer.measure(AnnotatedString(text), labelStyle)
            val textX = (x - layout.size.width / 2f)
                .coerceIn(plotStart, (plotEnd - layout.size.width).coerceAtLeast(plotStart))
            drawText(layout, topLeft = Offset(textX, y))
        }

        fun appendSmooth(path: Path, pts: List<Offset>) {
            for (i in 0 until pts.size - 1) {
                val p0 = pts[maxOf(i - 1, 0)]
                val p1 = pts[i]
                val p2 = pts[i + 1]
                val p3 = pts[minOf(i + 2, pts.lastIndex)]
                val c1 = Offset(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f)
                val c2 = Offset(p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f)
                path.cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
            }
        }

        fun drawSegment(from: Int, to: Int, color: Color, valueAt: (TrendSlot) -> Long?) {
            val pts = (from..to).map { i ->
                Offset(xMapped(i), geo.yPos(valueAt(slots[i])!!.toFloat()))
            }

            val fill = Path()
            fill.moveTo(pts.first().x, geo.baselineY)
            fill.lineTo(pts.first().x, pts.first().y)
            appendSmooth(fill, pts)
            fill.lineTo(pts.last().x, geo.baselineY)
            fill.close()
            drawPath(
                fill,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0f)),
                    startY = geo.top,
                    endY = geo.baselineY,
                ),
            )

            val line = Path()
            line.moveTo(pts.first().x, pts.first().y)
            appendSmooth(line, pts)
            drawPath(
                line,
                color,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            pts.forEach { p ->
                drawCircle(color, radius = 3.dp.toPx(), center = p)
                drawCircle(Color.White.copy(alpha = 0.85f), radius = 1.2.dp.toPx(), center = p)
            }
        }

        fun drawSeries(color: Color, valueAt: (TrendSlot) -> Long?) {
            var start = -1
            for (i in slots.indices) {
                val has = valueAt(slots[i]) != null
                if (has && start < 0) start = i
                if (!has && start >= 0) {
                    drawSegment(start, i - 1, color, valueAt)
                    start = -1
                }
            }
            if (start >= 0) drawSegment(start, slots.lastIndex, color, valueAt)
        }

        // Horizontal gridlines + Y labels (in seconds, 2 decimals)
        val levels = listOf(geo.yMin, (geo.yMin + geo.yMax) / 2f, geo.yMax)
        levels.forEach { v ->
            val y = geo.yPos(v)
            drawLine(gridColor, Offset(plotStart, y), Offset(plotEnd, y), strokeWidth = 1f)
            val layout = textMeasurer.measure(AnnotatedString(formatAxisSeconds(v)), labelStyle)
            drawText(layout, topLeft = Offset(plotStart - layout.size.width - 6f, y - layout.size.height / 2f))
        }

        // Baseline axis
        drawLine(axisLine, Offset(plotStart, geo.baselineY), Offset(plotEnd, geo.baselineY), strokeWidth = 1f)

        // X labels: all for small charts, otherwise thinned; never let the last two overlap
        val slotW = geo.plotW / geo.n
        val step = when {
            geo.n <= 10 -> 1
            slotW >= 40.dp.toPx() -> 1
            else -> ceil(geo.n / 6f).toInt().coerceAtLeast(1)
        }
        val labelIndices = mutableListOf<Int>()
        for (i in 0 until geo.n step step) labelIndices.add(i)
        if (labelIndices.last() != geo.n - 1) {
            val minSpacingPx = 26.dp.toPx()
            val lastGap = (geo.n - 1 - labelIndices.last()) * slotW
            if (lastGap >= minSpacingPx) {
                labelIndices.add(geo.n - 1)
            } else {
                labelIndices[labelIndices.lastIndex] = geo.n - 1
            }
        }
        labelIndices.forEach { i ->
            drawAxisLabel(slots[i].label, xMapped(i), geo.baselineY + 8f)
        }

        // Two series: 4×4 and 5×5
        drawSeries(fourColor) { it.fourAvgMs }
        drawSeries(fiveColor) { it.fiveAvgMs }

        // Dashed average line
        averageMs?.let { avg ->
            val avgY = geo.yPos(avg.toFloat())
            drawLine(
                color = avgColor,
                start = Offset(plotStart, avgY),
                end = Offset(plotEnd, avgY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
            )
            val layout = textMeasurer.measure(AnnotatedString("平均 ${formatSeconds(avg)}"), avgStyle)
            val labelY = (avgY - layout.size.height - 2.dp.toPx()).coerceAtLeast(0f)
            drawText(layout, topLeft = Offset(plotEnd - layout.size.width, labelY))
        }

        // Selected point highlight + tooltip
        selected?.let { (series, index) ->
            val slot = slots.getOrNull(index) ?: return@let
            val value = (if (series == 0) slot.fourAvgMs else slot.fiveAvgMs) ?: return@let
            val center = Offset(xMapped(index), geo.yPos(value.toFloat()))
            val seriesColor = if (series == 0) fourColor else fiveColor
            drawCircle(Color.White.copy(alpha = 0.95f), radius = 7.dp.toPx(), center = center)
            drawCircle(seriesColor, radius = 4.5.dp.toPx(), center = center)
            drawTooltip(
                text = if (tooltipShowLabel) "${slot.label} · ${formatSeconds(value)}" else formatSeconds(value),
                at = center,
                minX = plotStart,
                maxX = plotEnd,
                textMeasurer = textMeasurer,
                style = tooltipStyle,
                bubble = bubbleColor,
            )
        }
    }
}

private fun DrawScope.drawTooltip(
    text: String,
    at: Offset,
    minX: Float,
    maxX: Float,
    textMeasurer: TextMeasurer,
    style: TextStyle,
    bubble: Color,
) {
    val layout = textMeasurer.measure(AnnotatedString(text), style)
    val padX = 10.dp.toPx()
    val padY = 5.dp.toPx()
    val w = layout.size.width + padX * 2
    val h = layout.size.height + padY * 2
    val x = (at.x - w / 2f).coerceIn(minX, (maxX - w).coerceAtLeast(minX))
    val yAbove = at.y - h - 10.dp.toPx()
    val y = if (yAbove >= 0) yAbove else at.y + 12.dp.toPx()
    drawRoundRect(
        color = bubble,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(8.dp.toPx()),
    )
    drawText(layout, topLeft = Offset(x + padX, y + padY))
}

/** Formats an axis value given in milliseconds as seconds with 2 decimals. */
private fun formatAxisSeconds(v: Float): String =
    String.format(Locale.US, "%.2f秒", round(v / 1000.0 * 100.0) / 100.0)

private fun formatSeconds(ms: Long): String =
    String.format(Locale.US, "%.2f秒", round(ms / 1000.0 * 100.0) / 100.0)
