package com.relic.presentation.base

import android.support.design.widget.BottomSheetDialogFragment
import com.relic.dagger.Injectable

open class RelicBottomSheetDialog : BottomSheetDialogFragment(), Injectable {
    val TAG : String = javaClass.toString().split(".").last().toUpperCase()
}