package com.relic.presentation.base

import android.support.v4.app.Fragment
import android.util.Log
import com.relic.R

open class RelicFragment: Fragment() {
    protected val TAG : String = javaClass.toString().split(".").last()

    protected fun log(message : String) {
        Log.d(TAG, message)
    }

    protected fun transitionToFragment(fragment : Fragment, replace : Boolean = false) {
        activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_content_frame, fragment)
                .addToBackStack(TAG)
                .commit()
    }
}