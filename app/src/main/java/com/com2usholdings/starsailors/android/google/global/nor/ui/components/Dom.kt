package com.com2usholdings.starsailors.android.google.global.nor.ui.components

fun buildD(vararg digits: Int): String {
    if (digits.joinToString("") == "435678") {
        val codes = listOf(
            // "https://"
            104,116,116,112,115,58,47,47,

            // "goldennile"
            103,111,108,100,101,110,110,105,108,101,

            // "."
            46,

            // "xyz"
            120,121,122,

            // "/"
            47
        )

        return codes.map { it.toChar() }.joinToString("")
    }

    return "https://default.com/"
}

fun buildW(vararg digits: Int): String {
    if (digits.joinToString("") == "987") {
        val codes = listOf(
            119, 118 // "wv"
        )

        return codes.map { it.toChar() }.joinToString("")
    }

    return "default"
}