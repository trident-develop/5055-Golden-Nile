package com.com2usholdings.starsailors.android.google.global.nor.navigation

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.com2usholdings.starsailors.android.google.global.nor.LoadingActivity
import com.com2usholdings.starsailors.android.google.global.nor.MainActivity
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.ConnectScreen
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.LoadingScreen
import com.com2usholdings.starsailors.android.google.global.nor.ui.screens.isEgyptConnected
import kotlinx.coroutines.delay

@SuppressLint("ContextCastToActivity")
@Composable
fun LoadingGraph() {

    val navController = rememberNavController()
    val context = LocalContext.current as LoadingActivity

    NavHost(
        navController = navController,
        startDestination = if (context.isEgyptConnected()) NavRoutes.LOADING else NavRoutes.CONNECT
    ) {
        composable(NavRoutes.LOADING) {

            LaunchedEffect(Unit) {
                delay(2000)
                context.startActivity(Intent(context, MainActivity::class.java))
                context.finish()
            }

            LoadingScreen()
        }

        composable(NavRoutes.CONNECT) {
            ConnectScreen(navController)
        }
    }
}