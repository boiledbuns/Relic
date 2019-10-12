package com.relic.presentation.subsyncconfig

import com.relic.presentation.base.RelicViewModel
import kotlin.coroutines.CoroutineContext

class SubSyncConfigVM(
  private val subSyncPreferencesManager: SubSyncPM
) : RelicViewModel(){

    class Factory(
      private val subSyncPreferencesManager: SubSyncPM
    ) {
        fun create() : SubSyncConfigVM {
            return SubSyncConfigVM(subSyncPreferencesManager)
        }
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }





}