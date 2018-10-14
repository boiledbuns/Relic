package com.relic.presentation.subinfodialog

import android.arch.lifecycle.ViewModel
import com.relic.data.SubRepository
import javax.inject.Inject

class SubInfoDialogVM (
        private val subRepository: SubRepository,
        private val subredditName: String
) : ViewModel () {

    class Factory @Inject constructor(private val subRepository: SubRepository) {
        fun create(subredditName : String) {
            SubInfoDialogVM(subRepository, subredditName)
        }
    }

    init {
        //
    }

}