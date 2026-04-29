package com.com2usholdings.starsailors.android.google.global.nor.viewmodel

import androidx.lifecycle.ViewModel
import com.com2usholdings.starsailors.android.google.global.nor.event.StartDestination
import com.com2usholdings.starsailors.android.google.global.nor.event.StartSideEffect
import com.com2usholdings.starsailors.android.google.global.nor.event.StartState
import com.com2usholdings.starsailors.android.google.global.nor.event.TVEvent
import com.com2usholdings.starsailors.android.google.global.nor.game.ResolveStartFlowUseCase
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.privacy.TV3
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class StartViewModel(
    private val resolveStartFlowUseCase: ResolveStartFlowUseCase
) : ViewModel(), ContainerHost<StartState, StartSideEffect> {

    override val container = container<StartState, StartSideEffect>(
        initialState = StartState()
    )

    fun start() = intent {
        reduce {
            state.copy(isLoading = true)
        }

        val destination = resolveStartFlowUseCase()

        reduce {
            state.copy(isLoading = false)
        }

        when (destination) {
            is StartDestination.BuiltScore -> {
                postSideEffect(StartSideEffect.OpenBuiltScore(destination.score))
            }

            is StartDestination.OpenSavedScoreTypeA -> {
                postSideEffect(StartSideEffect.OpenTypeA(destination.score))
            }

            is StartDestination.OpenSavedScoreTypeB -> {
                postSideEffect(StartSideEffect.OpenTypeB(destination.score))
            }

            StartDestination.OpenGame -> {
                postSideEffect(StartSideEffect.OpenGame)
            }
        }
    }

    fun observeTVEvents(TV3: TV3) = intent {
        TV3.events.collect { event ->
            when (event) {
                TVEvent.OpenGame -> {
                    postSideEffect(StartSideEffect.OpenGame)
                }
            }
        }
    }
}