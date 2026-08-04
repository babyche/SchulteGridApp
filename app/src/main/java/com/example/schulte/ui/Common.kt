package com.example.schulte.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.schulte.ui.theme.WarmOrange
import com.example.schulte.ui.theme.WarmOrangeSoft

/**
 * Root background with soft ambient "light orbs" for a spatial, Vision Pro style feel.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        GlowOrb(
            size = 360.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 96.dp, y = (-80).dp),
        )
        GlowOrb(
            size = 320.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-96).dp, y = 150.dp),
        )
        GlowOrb(
            size = 220.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-70).dp, y = (-120).dp),
        )
        content()
    }
}

@Composable
private fun GlowOrb(size: Dp, color: Color, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val radius = with(density) { size.toPx() / 2f }
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), radius = radius))
    )
}

/**
 * Frosted floating card: super-ellipse corners, layered soft shadows (strong Z-depth),
 * a matte translucent surface, top specular gloss and a bright glass edge.
 * Slightly scales on press to reinforce the floating feel.
 */
@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    elevation: Dp = 22.dp,
    tint: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "floatingCard")

    // Light-sweep "shine" that glides across the card on every tap.
    var shineKey by remember { mutableIntStateOf(0) }
    val shine = remember { Animatable(0f) }
    LaunchedEffect(shineKey) {
        if (shineKey > 0) {
            shine.snapTo(0f)
            shine.animateTo(1f, tween(durationMillis = 680, easing = FastOutSlowInEasing))
        }
    }
    val shineProgress = shine.value
    val sweepCenterX = lerp(-0.45f, 1.45f, shineProgress)

    val effectiveOnClick = onClick?.let { c -> { shineKey += 1; c() } }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            // Wide, soft ambient shadow — the "float" beneath
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
            )
            // Medium diffuse shadow for mid-depth
            .shadow(
                elevation = 10.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.07f),
            )
            // Tight contact shadow
            .shadow(
                elevation = 3.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.12f),
            )
            .clip(shape)
            .background(tint)
            .border(1.dp, Color.White.copy(alpha = 0.72f), shape)
            .padding(contentPadding)
            .then(
                if (effectiveOnClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = effectiveOnClick,
                    )
                } else Modifier
            )
    ) {
        content()
        // Top specular gloss: a soft light catching the upper edge (matte acrylic)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = 0.4f,
                    )
                )
        )
        // Animated light sweep across the surface (only for tappable cards)
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val sweepWidth = size.width * 0.55f
                        val centerX = sweepCenterX * size.width
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.22f),
                                    Color.Transparent,
                                ),
                                startX = centerX - sweepWidth / 2f,
                                endX = centerX + sweepWidth / 2f,
                            ),
                            size = size,
                        )
                    }
            )
        }
    }
}

/**
 * A raised "tile" used for previews/badges sitting on top of a floating card,
 * so inner content also reads a few layers above the surface.
 */
@Composable
fun RaisedTile(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    background: Brush,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.10f),
            )
            .clip(shape)
            .background(background)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                    startY = 0f,
                    endY = 0.35f,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Pill-shaped soft chip, used for filters. Selected chips get a warm-orange gradient.
 */
@Composable
fun SoftChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    val chipBrush: Brush = if (selected) {
        Brush.linearGradient(listOf(WarmOrangeSoft, WarmOrange))
    } else {
        val c = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        Brush.linearGradient(listOf(c, c))
    }
    Box(
        modifier = modifier
            .padding(vertical = 2.dp)
            .clip(shape)
            .background(chipBrush)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else Color.White.copy(alpha = 0.65f),
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun RoundIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "roundIcon")
    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 3.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.10f),
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun ArrowPill(
    accent: Brush,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(size / 2),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.12f),
            )
            .clip(RoundedCornerShape(size / 2))
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
