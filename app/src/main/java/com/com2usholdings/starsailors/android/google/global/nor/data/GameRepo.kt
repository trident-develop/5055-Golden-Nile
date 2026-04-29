package com.com2usholdings.starsailors.android.google.global.nor.data

interface GameRepo {
    suspend fun getSavedScore(): String?
    suspend fun saveScore(score: String)
    suspend fun isNotifyShown(): Boolean
    suspend fun markNotifyShown()
}