package com.relic.presentation.base

import android.support.design.widget.BottomSheetDialogFragment
import com.relic.dagger.RelicInjectable

open class RelicBottomSheetDialog : BottomSheetDialogFragment(), RelicInjectable {
    val TAG : String = javaClass.toString().split(".").last().toUpperCase()
}