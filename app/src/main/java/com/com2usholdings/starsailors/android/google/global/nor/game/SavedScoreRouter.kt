package com.com2usholdings.starsailors.android.google.global.nor.game

import com.com2usholdings.starsailors.android.google.global.nor.event.StartDestination
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.buildD

class SavedScoreRouter {

    fun score(savedScore: String): StartDestination {
        return when {
            !savedScore.startsWith(buildD(435678)) -> {
                StartDestination.OpenSavedScoreTypeA(savedScore)
            }

            savedScore.startsWith(buildD(435678)) -> {
                StartDestination.OpenSavedScoreTypeB(savedScore)
            }

            else -> {
                StartDestination.OpenGame
            }
        }
    }
}