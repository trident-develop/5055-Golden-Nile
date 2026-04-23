package com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game

import kotlin.random.Random

/** 2D view over a flat tile list, addressable by (row, col). */
internal fun tilesToGrid(tiles: List<Tile>): Array<Array<Tile?>> {
    val grid = Array(ROWS) { arrayOfNulls<Tile>(COLS) }
    for (t in tiles) {
        if (t.row in 0 until ROWS && t.col in 0 until COLS) grid[t.row][t.col] = t
    }
    return grid
}

/** Flood-fill same-symbol 4-neighbors starting at ([row], [col]). */
internal fun floodFillGroup(
    grid: Array<Array<Tile?>>,
    row: Int,
    col: Int,
): Set<Long> {
    val origin = grid.getOrNull(row)?.getOrNull(col) ?: return emptySet()
    val target = origin.symbol
    val result = mutableSetOf<Long>()
    val stack = ArrayDeque<Pair<Int, Int>>()
    stack.addLast(row to col)
    while (stack.isNotEmpty()) {
        val (r, c) = stack.removeLast()
        val t = grid.getOrNull(r)?.getOrNull(c) ?: continue
        if (t.symbol != target) continue
        if (t.id in result) continue
        result += t.id
        stack.addLast(r - 1 to c)
        stack.addLast(r + 1 to c)
        stack.addLast(r to c - 1)
        stack.addLast(r to c + 1)
    }
    return result
}

/** Every distinct connected-same-symbol group of size >= [minSize] on the board. */
internal fun findAllGroups(tiles: List<Tile>, minSize: Int): List<Set<Long>> {
    val grid = tilesToGrid(tiles)
    val seen = mutableSetOf<Long>()
    val groups = mutableListOf<Set<Long>>()
    for (r in 0 until ROWS) {
        for (c in 0 until COLS) {
            val t = grid[r][c] ?: continue
            if (t.id in seen) continue
            val group = floodFillGroup(grid, r, c)
            seen += group
            if (group.size >= minSize) groups += group
        }
    }
    return groups
}

/**
 * Apply gravity (surviving tiles fall to the bottom of their column) and refill the
 * empty cells at the top with new random tiles. New tiles get a [Tile.spawnRow] placed
 * above the board so their Animatable starts off-screen and falls down into place.
 */
internal fun applyGravityAndRefill(
    survivors: List<Tile>,
    random: Random,
    nextId: () -> Long,
): List<Tile> {
    val byCol = survivors.groupBy { it.col }
    val result = ArrayList<Tile>(ROWS * COLS)
    for (c in 0 until COLS) {
        val existing = (byCol[c] ?: emptyList()).sortedByDescending { it.row }
        existing.forEachIndexed { i, t ->
            val newRow = ROWS - 1 - i
            result += t.copy(row = newRow, popping = false, spawnRow = newRow.toFloat())
        }
        val missing = ROWS - existing.size
        for (i in 0 until missing) {
            val newRow = missing - 1 - i
            result += Tile(
                id = nextId(),
                symbol = random.nextInt(SYMBOL_COUNT),
                row = newRow,
                col = c,
                spawnRow = (newRow - missing).toFloat(),
            )
        }
    }
    return result
}

/** Generate a fresh board. Every tile spawns above the board for a satisfying intro. */
internal fun buildInitialBoard(
    random: Random,
    nextId: () -> Long,
): List<Tile> {
    val list = ArrayList<Tile>(ROWS * COLS)
    for (r in 0 until ROWS) {
        for (c in 0 until COLS) {
            list += Tile(
                id = nextId(),
                symbol = random.nextInt(SYMBOL_COUNT),
                row = r,
                col = c,
                spawnRow = (r - ROWS).toFloat(),
            )
        }
    }
    return list
}

/**
 * Guarantee at least one tappable pair exists. With 7 symbols on 48 cells this almost
 * never triggers, but if it does we re-colour one random neighbour rather than stall
 * the player.
 */
internal fun ensureTappableBoard(
    tiles: List<Tile>,
    random: Random,
): List<Tile> {
    if (findAllGroups(tiles, minSize = 2).isNotEmpty()) return tiles
    val grid = tilesToGrid(tiles)
    val r = random.nextInt(ROWS)
    val c = random.nextInt(COLS - 1)
    val a = grid[r][c] ?: return tiles
    val b = grid[r][c + 1] ?: return tiles
    return tiles.map { if (it.id == b.id) it.copy(symbol = a.symbol) else it }
}
