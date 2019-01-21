package com.relic.presentation.base

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.content.res.TypedArray
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.relic.MainActivity
import com.relic.R

open class RelicFragment: Fragment() {
    protected val TAG : String = javaClass.toString().split(".").last().toUpperCase()

    private val connectivityManager: ConnectivityManager by lazy {
        requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var override = true

        item?.itemId?.let { id ->
            when (id) {
                android.R.id.home -> activity?.onBackPressed()
                else -> override = super.onOptionsItemSelected(item)
            }
        }

        return override
    }

    protected open fun bindViewModel(lifecycleOwner : LifecycleOwner) { }

    protected fun checkInternetConnectivity() : Boolean {
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}