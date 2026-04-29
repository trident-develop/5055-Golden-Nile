package com.com2usholdings.starsailors.android.google.global.nor.game

import com.com2usholdings.starsailors.android.google.global.nor.model.ScoreParams

class ScoreParamsCollector(
    private val signalsProvider: DeviceSignalsProvider
) {

    suspend fun collect(): ScoreParams {
        val signals = signalsProvider.collect()

        return ScoreParams(
            referrer = signals.referrer,
            gadid = signals.gadid,
            probe = signals.probe,
            device = signals.deviceName,
            firebaseId = signals.firebaseId,
            installTime = signals.installTime
        )
    }
}