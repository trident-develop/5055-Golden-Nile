package com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game

import androidx.annotation.DrawableRes
import com.com2usholdings.starsailors.android.google.global.nor.R

/** Board geometry. 6 columns × 8 rows reads well in portrait on phones from 360dp up. */
const val COLS = 6
const val ROWS = 8

/** Symbol palette — maps to egypt_el_1…egypt_el_7. */
const val SYMBOL_COUNT = 7

@DrawableRes
fun drawableForSymbol(symbol: Int): Int = when (symbol) {
    0 -> R.drawable.egypt_el_1
    1 -> R.drawable.egypt_el_2
    2 -> R.drawable.egypt_el_3
    3 -> R.drawable.egypt_el_4
    4 -> R.drawable.egypt_el_5
    5 -> R.drawable.egypt_el_6
    else -> R.drawable.egypt_el_7
}

/**
 * One tile on the board. Stable [id] lets Compose key the tile across recompositions so
 * moves/spawns animate smoothly. [spawnRow] is the initial visual row used by the fall
 * animation — only meaningful on the tile's very first composition.
 */
data class Tile(
    val id: Long,
    val symbol: Int,
    val row: Int,
    val col: Int,
    val popping: Boolean = false,
    val spawnRow: Float = row.toFloat(),
)

/** Per-level difficulty. Target grows, moves shrink — but the game stays beatable. */
data class LevelConfig(
    val targetScore: Int,
    val maxMoves: Int,
) {
    companion object {
        fun forLevel(level: Int): LevelConfig {
            val lvl = level.coerceAtLeast(1)
            val targetScore = 350 + (lvl - 1) * 110
            val maxMoves = (22 - (lvl - 1) / 5).coerceIn(14, 22)
            return LevelConfig(targetScore = targetScore, maxMoves = maxMoves)
        }
    }
}

/**
 * Score for clearing [size] connected tiles on the [chain]-th pop of the current turn
 * (1 = the player's tap, 2+ = auto-cascade). Quadratic base, flat bonuses for 5+ / 7+,
 * and a combo multiplier for cascades.
 */
fun scoreForGroup(size: Int, chain: Int): Int {
    if (size < 2) return 0
    val base = 10 * size * (size - 1)
    val big = (if (size >= 5) 50 else 0) + (if (size >= 7) 100 else 0)
    val multiplier = 1f + 0.5f * (chain - 1).coerceAtLeast(0)
    return ((base + big) * multiplier).toInt()
}

/** Pop/fall timing. Short on purpose — the game needs to feel snappy, not cinematic. */
const val POP_MS = 180
const val FALL_MS = 280
