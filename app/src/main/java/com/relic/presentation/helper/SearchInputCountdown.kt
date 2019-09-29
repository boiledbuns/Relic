package com.relic.presentation.helper

import android.os.CountDownTimer

// TODO: allow the use to tune these values
const val millisCountdown = 550L
const val millisTick = 2000L

class SearchInputCountdown(
    private val callback : () -> Unit
) : CountDownTimer(millisCountdown, millisTick) {

    override fun onFinish() = callback()

    // do nothing for the ticks
    override fun onTick(millisUntilFinished: Long) {}
}