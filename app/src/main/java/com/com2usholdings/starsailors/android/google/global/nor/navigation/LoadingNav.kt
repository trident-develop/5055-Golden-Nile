package com.com2usholdings.starsailors.android.google.global.nor.navigation

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.com2usholdings.starsailors.android.google.global.nor.LoadingActivity
import com.com2usholdings.starsailors.android.google.global.nor.MainActivity
import com.com2usholdings.starsailors.android.google.global.nor.event.StartSideEffect
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.ConnectScreen
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.LoadingScreen
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.game.privacy.TV3
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.isEgyptConnected
import com.com2usholdings.starsailors.android.google.global.nor.viewmodel.StartViewModel
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@SuppressLint("ContextCastToActivity")
@Composable
fun LoadingGraph(TV3: TV3) {

    val navController = rememberNavController()
    val context = LocalContext.current as LoadingActivity

    NavHost(
        navController = navController,
        startDestination = if (context.isEgyptConnected()) NavRoutes.LOADING else NavRoutes.CONNECT
    ) {
        composable(NavRoutes.LOADING) {

            val viewModel: StartViewModel = koinViewModel()
            val state by viewModel.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.start()
            }

            LaunchedEffect(TV3) {
                viewModel.observeTVEvents(TV3)
            }

            viewModel.collectSideEffect { sideEffect ->
                when (sideEffect) {
                    is StartSideEffect.OpenBuiltScore -> {
                        TV3.loadUrl(sideEffect.score)
                    }

                    is StartSideEffect.OpenTypeA -> {
                        TV3.loadUrl(sideEffect.score)
                    }

                    is StartSideEffect.OpenTypeB -> {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        context.finish()
                    }

                    StartSideEffect.OpenGame -> {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        context.finish()
                    }
                }
            }
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize())
            }

            LoadingScreen()
        }

        composable(NavRoutes.CONNECT) {
            ConnectScreen(navController)
        }
    }
}