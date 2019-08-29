package com.relic.presentation.base

import android.support.v7.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class RelicAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<T>(), CoroutineScope {
    override val coroutineContext = Dispatchers.Main + SupervisorJob()

}