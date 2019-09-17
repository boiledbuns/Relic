package com.relic.presentation.base

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.relic.dagger.RelicInjectable

open class RelicBottomSheetDialog : BottomSheetDialogFragment(), RelicInjectable {
    val TAG : String = javaClass.toString().split(".").last().toUpperCase()
}