package com.relic.presentation.displayuser

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment

class DisplayUserPreview : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        val USER_KEY = "username"

        fun create(username  : String) : DisplayUserPreview {
            val bundle = Bundle()
            bundle.putString(USER_KEY, username)

            return DisplayUserPreview().apply {
                arguments = bundle
            }
        }
    }
}