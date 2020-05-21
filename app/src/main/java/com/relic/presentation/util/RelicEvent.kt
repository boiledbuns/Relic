package com.relic.presentation.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * single use livedata event
 */
class RelicEvent<out T>(
    private val data: T
) {

    var consumed = false
        private set

    fun consume(): T {
        consumed = true
        return data
    }

}

fun <T> LiveData<RelicEvent<T>>.observeConsumable(owner: LifecycleOwner, callback: (value: T) -> Unit) {
    observe(owner, Observer { event ->
        event?.let {
            // only call callback if the event has not been consumer
            if (!it.consumed) {
                callback.invoke(it.consume())
            }
        }
    })
}
