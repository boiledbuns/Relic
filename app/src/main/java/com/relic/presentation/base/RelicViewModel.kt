package com.relic.presentation.base

import android.arch.lifecycle.ViewModel
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class RelicViewModel : ViewModel(), CoroutineScope {
    protected val TAG : String = javaClass.toString().split(".").last().toUpperCase()

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        handleException(context, e)
        Log.e(TAG, "caught exception", e)
    }

    abstract fun handleException(context: CoroutineContext, e : Throwable)
}