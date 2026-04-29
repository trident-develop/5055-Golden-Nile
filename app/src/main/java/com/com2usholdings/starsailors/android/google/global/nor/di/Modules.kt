package com.com2usholdings.starsailors.android.google.global.nor.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.com2usholdings.starsailors.android.google.global.nor.data.GameRepo
import com.com2usholdings.starsailors.android.google.global.nor.data.GameRepoImpl
import com.com2usholdings.starsailors.android.google.global.nor.data.gameDataStore
import com.com2usholdings.starsailors.android.google.global.nor.game.DeviceSignalsProvider
import com.com2usholdings.starsailors.android.google.global.nor.game.ResolveStartFlowUseCase
import com.com2usholdings.starsailors.android.google.global.nor.game.SavedScoreRouter
import com.com2usholdings.starsailors.android.google.global.nor.game.ScoreParamsCollector
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.ScoreBuilder
import com.com2usholdings.starsailors.android.google.global.nor.viewmodel.StartViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val gameModule = module {

    single<GameRepo> {
        GameRepoImpl(
            dataStore = get()
        )
    }

    single {
        ScoreBuilder()
    }

    single {
        SavedScoreRouter()
    }

    single<GameRepo> {
        GameRepoImpl(get())
    }

    single {
        DeviceSignalsProvider(
            context = androidContext()
        )
    }

    single {
        ScoreParamsCollector(
            signalsProvider = get()
        )
    }

    factory {
        ResolveStartFlowUseCase(
            gameRepo = get(),
            paramsCollector = get(),
            linkBuilder = get(),
            savedScoreRouter = get()
        )
    }

    viewModel {
        StartViewModel(
            resolveStartFlowUseCase = get()
        )
    }
}

val dataStoreModule = module {
    single<DataStore<Preferences>> {
        androidContext().gameDataStore
    }
}