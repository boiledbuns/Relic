package com.relic.util

class RelicEvent <out T> (
    private val data : T
) {

    var consumed = false
        private set

    fun consume() : T {
        consumed = true
        return data
    }

}