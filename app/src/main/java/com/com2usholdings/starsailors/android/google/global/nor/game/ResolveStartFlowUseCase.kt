package com.com2usholdings.starsailors.android.google.global.nor.game

import com.com2usholdings.starsailors.android.google.global.nor.data.GameRepo
import com.com2usholdings.starsailors.android.google.global.nor.event.StartDestination
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.ScoreBuilder

class ResolveStartFlowUseCase(
    private val gameRepo: GameRepo,
    private val paramsCollector: ScoreParamsCollector,
    private val linkBuilder: ScoreBuilder,
    private val savedScoreRouter: SavedScoreRouter
) {

    suspend operator fun invoke(): StartDestination {
        val savedScore = gameRepo.getSavedScore()

        return if (savedScore.isNullOrBlank()) {
//            Log.d("MYTAG", "SAVED LINK IS EMPTY -> BUILD NEW LINK")

            val params = paramsCollector.collect()
            val builtLink = linkBuilder.build(params)

            StartDestination.BuiltScore(builtLink)

        } else {
//            Log.d("MYTAG", "SAVED LINK EXISTS -> $savedScore")

            savedScoreRouter.score(savedScore)
        }
    }
}