package com.relic.presentation.base

import android.arch.lifecycle.LifecycleOwner
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import com.relic.R

open class RelicFragment: Fragment() {
    protected val TAG : String = javaClass.toString().split(".").last().toUpperCase()

    protected fun log(message : String) {
        Log.d(TAG, message)
    }

    protected fun dismiss() {
        childFragmentManager.popBackStackImmediate()
    }

    protected fun transitionToFragment(fragment : Fragment, replace : Boolean = false) {
        activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_content_frame, fragment)
                .addToBackStack(TAG)
                .commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel(viewLifecycleOwner)
    }

    protected open fun bindViewModel(lifecycleOwner : LifecycleOwner) { }
}