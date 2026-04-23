package com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.com2usholdings.starsailors.android.google.global.nor.audio.GameAudioController
import com.com2usholdings.starsailors.android.google.global.nor.audio.LocalAudioManager
import com.com2usholdings.starsailors.android.google.global.nor.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameResult { Win, Lose }

/**
 * Drives the match-collapse board. State is exposed as Compose observables so the
 * screen can re-render reactively. All mutations happen on the main dispatcher via
 * the provided [scope].
 */
class GameController(
    val level: Int,
    private val audio: GameAudioController,
    private val prefs: PreferencesManager,
    private val scope: CoroutineScope,
) {
    private var random: Random = Random(System.currentTimeMillis())
    private var nextTileId = 0L
    private val allocId: () -> Long = { nextTileId++ }
    private var resultPersisted = false

    var tiles by mutableStateOf<List<Tile>>(emptyList())
        private set
    var score by mutableIntStateOf(0)
        private set
    var targetScore by mutableIntStateOf(0)
        private set
    var movesLeft by mutableIntStateOf(0)
        private set
    var busy by mutableStateOf(false)
        private set
    var result by mutableStateOf<GameResult?>(null)
        private set

    /** Last score gain + a monotonically-increasing key so the "+N" badge re-animates. */
    var lastGain by mutableIntStateOf(0)
        private set
    var lastGainKey by mutableIntStateOf(0)
        private set

    /** Bumps when the player taps a single tile — so the UI can play a "no-op" shake. */
    var shakeKey by mutableIntStateOf(0)
        private set

    init { reset() }

    fun reset() {
        val config = LevelConfig.forLevel(level)
        targetScore = config.targetScore
        movesLeft = config.maxMoves
        score = 0
        busy = false
        result = null
        lastGain = 0
        lastGainKey = 0
        shakeKey = 0
        resultPersisted = false
        random = Random(System.currentTimeMillis())
        nextTileId = 0L
        tiles = ensureTappableBoard(
            buildInitialBoard(random = random, nextId = allocId),
            random = random,
        )
    }

    fun onTap(row: Int, col: Int) {
        if (busy || result != null) return
        val grid = tilesToGrid(tiles)
        val group = floodFillGroup(grid, row, col)
        if (group.size < 2) {
            shakeKey += 1
            return
        }
        scope.launch { runTurn(group) }
    }

    private suspend fun runTurn(firstGroup: Set<Long>) {
        busy = true
        try {
            popGroup(firstGroup)
            movesLeft = (movesLeft - 1).coerceAtLeast(0)
            tiles = ensureTappableBoard(tiles, random)
            delay(120L)
            evaluateResult()
        } finally {
            busy = false
        }
    }

    private suspend fun popGroup(group: Set<Long>) {
        val gained = scoreForGroup(group.size, chain = 1)
        score += gained
        lastGain = gained
        lastGainKey += 1
        audio.playSpin()

        tiles = tiles.map { if (it.id in group) it.copy(popping = true) else it }
        delay(POP_MS.toLong())

        val survivors = tiles.filterNot { it.id in group }
        tiles = applyGravityAndRefill(survivors, random, allocId)
        delay(FALL_MS.toLong())
        // No auto-cascade — any groups formed by the fall wait for the player's next tap.
    }

    private fun evaluateResult() {
        if (result != null) return
        when {
            score >= targetScore -> {
                result = GameResult.Win
                if (!resultPersisted) {
                    prefs.registerWin(level, score)
                    resultPersisted = true
                }
                audio.playWin()
            }
            movesLeft <= 0 -> {
                result = GameResult.Lose
                if (!resultPersisted) {
                    prefs.registerLoss(level, score)
                    resultPersisted = true
                }
                audio.playLose()
            }
        }
    }
}

@Composable
fun rememberGameController(level: Int): GameController {
    val context = LocalContext.current
    val audio = LocalAudioManager.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferencesManager.get(context) }
    return remember(level) {
        GameController(level = level, audio = audio, prefs = prefs, scope = scope)
    }
}
