package com.relic.presentation.base

import android.arch.lifecycle.ViewModel
import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class RelicViewModel : ViewModel(), CoroutineScope {
    protected val TAG : String = javaClass.toString().split(".").last().toUpperCase()

    override val coroutineContext = Dispatchers.Main + CoroutineName(TAG) + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        handleException(context, e)
        Log.e(TAG, "caught exception", e)
    }

    abstract fun handleException(context: CoroutineContext, e : Throwable)
}