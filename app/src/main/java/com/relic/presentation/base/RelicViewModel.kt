package com.relic.presentation.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

abstract class RelicViewModel : ViewModel(), CoroutineScope {
    protected val TAG : String = javaClass.toString().split(".").last().toUpperCase()

    override val coroutineContext = Dispatchers.Main + CoroutineName(TAG) + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        handleException(context, e)
        Timber.e(e,  "caught exception")
    }

    abstract fun handleException(context: CoroutineContext, e : Throwable)
}