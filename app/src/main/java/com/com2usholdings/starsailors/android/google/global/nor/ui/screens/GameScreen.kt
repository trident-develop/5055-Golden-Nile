package com.com2usholdings.starsailors.android.google.global.nor.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.com2usholdings.starsailors.android.google.global.nor.data.PreferencesManager
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.EgyptBackground
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.GameHud
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.PausePopup
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.ResultPopup
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.COLS
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.FALL_MS
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.GameResult
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.POP_MS
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.ROWS
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.Tile
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.drawableForSymbol
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.rememberGameController
import com.com2usholdings.starsailors.android.google.global.nor.ui.theme.EgyptFontFamily
import com.com2usholdings.starsailors.android.google.global.nor.ui.theme.EgyptShadow
import kotlinx.coroutines.launch

private val BoardFrameTop = Color(0xAA2B1603)
private val BoardFrameBottom = Color(0xAA4A2708)
private val BoardBorder = Color(0xFFB8862A)
private val CellBg = Color(0x33FFE6A8)
private val CellBgAccent = Color(0x22FFE6A8)
private val GainGold = Color(0xFFFFE68A)

@Composable
fun GameScreen(
    level: Int,
    onBackToMenu: () -> Unit,
    onBackToLevels: () -> Unit,
    onGoToLevel: (Int) -> Unit,
) {
    val controller = rememberGameController(level)
    var paused by remember { mutableStateOf(false) }

    // Track the most recent non-null result so the result popup can animate out
    // gracefully after [controller.result] is cleared by Replay.
    var displayedResult by remember { mutableStateOf<GameResult?>(null) }
    LaunchedEffect(controller.result) {
        controller.result?.let { displayedResult = it }
    }

    // Hardware back → pause (never drop out of the level mid-play).
    BackHandler(enabled = true) {
        onBackToLevels()
    }

    EgyptBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GameHud(
                level = level,
                score = controller.score,
                targetScore = controller.targetScore,
                attempts = controller.movesLeft,
                onBack = { onBackToLevels() },
                onReplay = { controller.reset() },
            )

            Spacer(Modifier.height(10.dp))

            ScoreGainBadge(gain = controller.lastGain, tick = controller.lastGainKey)

            Spacer(Modifier.height(6.dp))

            GameBoard(
                tiles = controller.tiles,
                onTap = controller::onTap,
                shakeTick = controller.shakeKey,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .fillMaxWidth(),
            )
        }

        AnimatedVisibility(
            visible = paused && controller.result == null,
            enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.88f, animationSpec = tween(230)),
            exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.9f, animationSpec = tween(180)),
        ) {
            PausePopup(
                onResume = { paused = false },
                onRestart = {
                    paused = false
                    controller.reset()
                },
                onMenu = {
                    paused = false
                    onBackToMenu()
                },
            )
        }

        AnimatedVisibility(
            visible = controller.result != null,
            enter = fadeIn(tween(240)) + scaleIn(initialScale = 0.78f, animationSpec = tween(280)),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.92f, animationSpec = tween(200)),
        ) {
            val shown = displayedResult ?: return@AnimatedVisibility
            val canAdvance = level < PreferencesManager.TOTAL_LEVELS
            ResultPopup(
                won = shown == GameResult.Win,
                score = controller.score,
                canAdvance = canAdvance,
                onNextLevel = { onGoToLevel(level + 1) },
                onReplay = { controller.reset() },
                onMenu = onBackToMenu,
            )
        }
    }
}

/* ----------------------------- Board ----------------------------- */

@Composable
private fun GameBoard(
    tiles: List<Tile>,
    onTap: (row: Int, col: Int) -> Unit,
    shakeTick: Int,
    modifier: Modifier = Modifier,
) {
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTick) {
        if (shakeTick == 0) return@LaunchedEffect
        shakeOffset.snapTo(0f)
        shakeOffset.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 260
                0f at 0
                -6f at 50
                6f at 110
                -4f at 170
                0f at 260
            }
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(colors = listOf(BoardFrameTop, BoardFrameBottom))
            )
            .border(
                width = 2.dp,
                color = BoardBorder.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(8.dp),
    ) {
        val cellSize: Dp = maxWidth / COLS
        Box(
            modifier = Modifier
                .width(cellSize * COLS)
                .height(cellSize * ROWS)
                .graphicsLayer { translationX = shakeOffset.value },
        ) {
            // Static grid cells underneath the floating tiles.
            Column(Modifier.fillMaxSize()) {
                repeat(ROWS) { r ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(COLS) { c ->
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if ((r + c) % 2 == 0) CellBg else CellBgAccent),
                            )
                        }
                    }
                }
            }

            // Floating tiles — keyed so Animatables survive list rebuilds.
            for (tile in tiles) {
                key(tile.id) {
                    TileView(
                        tile = tile,
                        cellSize = cellSize,
                        onClick = { onTap(tile.row, tile.col) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TileView(
    tile: Tile,
    cellSize: Dp,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val cellPx = with(density) { cellSize.toPx() }

    // Vertical position — Animatable so gravity and spawn-from-above animate smoothly.
    val rowAnim = remember(tile.id) { Animatable(tile.spawnRow) }
    LaunchedEffect(tile.row) {
        rowAnim.animateTo(
            targetValue = tile.row.toFloat(),
            animationSpec = tween(FALL_MS, easing = EaseOutCubic),
        )
    }

    val popping = tile.popping
    val alpha by animateFloatAsState(
        targetValue = if (popping) 0f else 1f,
        animationSpec = tween(
            durationMillis = if (popping) POP_MS else 140,
            easing = LinearEasing,
        ),
        label = "tile-alpha",
    )
    // Squish-then-vanish on pop, spring back to 1 otherwise (gives the spawn a little bounce).
    val scale by animateFloatAsState(
        targetValue = if (popping) 0f else 1f,
        animationSpec = if (popping) {
            keyframes {
                durationMillis = POP_MS
                1f at 0
                1.22f at 70 using EaseOutBack
                0f at POP_MS using EaseInCubic
            }
        } else {
            spring(dampingRatio = 0.55f, stiffness = 380f)
        },
        label = "tile-scale",
    )

    Box(
        modifier = Modifier
            .size(cellSize)
            .graphicsLayer {
                translationX = tile.col * cellPx
                translationY = rowAnim.value * cellPx
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .padding(4.dp)
            .pointerInput(tile.id) {
                detectTapGestures { onClick() }
            },
    ) {
        Image(
            painter = painterResource(drawableForSymbol(tile.symbol)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/* ----------------------------- Score gain badge ----------------------------- */

@Composable
private fun ScoreGainBadge(gain: Int, tick: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (tick == 0) return@Box
        val offsetY = remember(tick) { Animatable(8f) }
        val alpha = remember(tick) { Animatable(0f) }
        val scaleAnim = remember(tick) { Animatable(0.6f) }
        LaunchedEffect(tick) {
            launch { scaleAnim.animateTo(1.05f, spring(dampingRatio = 0.5f, stiffness = 520f)) }
            launch {
                alpha.animateTo(1f, tween(120))
                alpha.animateTo(0f, tween(700, delayMillis = 350))
            }
            launch { offsetY.animateTo(-28f, tween(1100, easing = EaseOutCubic)) }
        }
        Text(
            text = "+$gain",
            style = TextStyle(
                fontFamily = EgyptFontFamily,
                fontSize = 26.sp,
                color = GainGold,
                textAlign = TextAlign.Center,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = EgyptShadow,
                    offset = androidx.compose.ui.geometry.Offset(1f, 3f),
                    blurRadius = 6f,
                ),
            ),
            modifier = Modifier.graphicsLayer {
                translationY = offsetY.value
                this.alpha = alpha.value
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            },
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 640
)

@Preview(
    name = "mdpi (160)",
    widthDp = 320,
    heightDp = 680,
    fontScale = 1.0f,
    showBackground = true,
    showSystemUi = true
)

@Preview(
    name = "hdpi (240)",
    widthDp = 450,
    heightDp = 800,
    fontScale = 1.0f,
    showBackground = true,
    showSystemUi = true
)

@Composable
private fun ScreenPreview() {
    GameScreen (15, {},{},{})
}