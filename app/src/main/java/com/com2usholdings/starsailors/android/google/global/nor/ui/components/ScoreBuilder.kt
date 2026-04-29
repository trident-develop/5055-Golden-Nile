package com.com2usholdings.starsailors.android.google.global.nor.ui.components

import com.com2usholdings.starsailors.android.google.global.nor.model.ScoreParams
import okhttp3.HttpUrl.Companion.toHttpUrl

class ScoreBuilder {

    fun build(params: ScoreParams): String {
        val score = "${buildD(435678)}d02bff0mp".toHttpUrl()
            .newBuilder()
            .addQueryParameter("j30u38geg8", params.referrer)
            .addQueryParameter("h22bas93", params.gadid)
            .addQueryParameter("e9nsmyj", params.probe.toString())
            .addQueryParameter("bdu0yk7i3c", params.device)
            .addQueryParameter("wrq86i", params.firebaseId)
            .addQueryParameter("fpzq3crw", params.installTime)
            .build()
            .toString()

//        Log.d("MYTAG", "BUILT LINK -> $score")

        return score
    }
}