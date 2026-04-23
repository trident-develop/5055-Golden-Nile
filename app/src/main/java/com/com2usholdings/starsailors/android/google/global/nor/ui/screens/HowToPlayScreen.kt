package com.com2usholdings.starsailors.android.google.global.nor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.com2usholdings.starsailors.android.google.global.nor.R
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.EgyptBackground
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.EgyptBodyText
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.IconImageButton
import com.com2usholdings.starsailors.android.google.global.nor.ui.components.ScreenTitle
import com.com2usholdings.starsailors.android.google.global.nor.ui.theme.EgyptGold
import com.com2usholdings.starsailors.android.google.global.nor.ui.theme.EgyptGoldDeep
import com.com2usholdings.starsailors.android.google.global.nor.ui.theme.EgyptSand

private val PanelTextColor = Color(0xFF442A02)

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    EgyptBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconImageButton(
                    drawableRes = R.drawable.back_button,
                    onClick = onBack,
                    size = 52.dp,
                    contentDescription = "Back",
                )
                ScreenTitle(text = "How To Play", fontSize = 28)
                Spacer(Modifier.size(52.dp))
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EgyptSand,
                                Color(0xFFCFA369),
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(EgyptGold, EgyptGoldDeep)
                        ),
                        shape = RoundedCornerShape(20.dp),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    EgyptBodyText(
                        color = PanelTextColor,
                        text = "Welcome, seeker — the Sands of the Nile lie before you, " +
                                "buried in relics of the pharaohs."
                    )
                    EgyptBodyText(
                        color = PanelTextColor,
                        text = "The board is filled with ancient Egyptian symbols. Tap any " +
                                "group of two or more identical, connected symbols to clear them."
                    )
                    EgyptBodyText(
                        color = PanelTextColor,
                        text = "Neighbours count up, down, left and right — not diagonally. " +
                                "Lone symbols cannot be cleared, so look for clusters."
                    )
                    EgyptBodyText(
                        color = PanelTextColor,
                        text = "The bigger the group, the richer the reward. Groups of 5 earn " +
                                "a treasure bonus; groups of 7+ earn a pharaoh's bonus."
                    )
                    EgyptBodyText(
                        color = PanelTextColor,
                        text = "After a group vanishes, the symbols above collapse into the " +
                                "gaps and fresh ones cascade in from above — forming new " +
                                "clusters for your next tap."
                    )
                    EgyptBodyText(
                        color = PanelTextColor,
                        text = "Each tap costs one move. Reach the TARGET score before your " +
                                "moves run out to conquer the level."
                    )
                    EgyptBodyText(
                        color = PanelTextColor,
                        text = "Winning a level unlocks the next one. There are 33 levels, " +
                                "and every new one demands a little more than the last."
                    )
                    EgyptBodyText(
                        color = PanelTextColor,
                        text = "Plan your taps — the richest tombs belong to those who spot " +
                                "the biggest clusters first."
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}