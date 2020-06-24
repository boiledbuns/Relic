package com.relic.presentation.helper

import android.os.CountDownTimer

// TODO: allow the use to tune these values
const val millisCountdown = 550L
const val millisTick = 2000L

class SearchInputCountdown : CountDownTimer(millisCountdown, millisTick) {
    private var callback: (() -> Unit)? = null

    fun start(newCallBack: () -> Unit) {
        start()
        callback = newCallBack
    }

    override fun onFinish() {
        callback?.invoke()
    }

    // do nothing for the ticks
    override fun onTick(millisUntilFinished: Long) {}
}