package com.relic.data

import com.relic.presentation.callbacks.AuthenticationCallback

interface Auth {
    fun refreshToken(callback: AuthenticationCallback)
}