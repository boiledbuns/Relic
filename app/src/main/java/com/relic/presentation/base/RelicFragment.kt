package com.relic.presentation.base

import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import android.view.MenuItem
import android.view.View
import com.relic.R
import com.relic.dagger.RelicInjectable
import timber.log.Timber

open class RelicFragment: androidx.fragment.app.Fragment(), RelicInjectable {
    protected val TAG : String = javaClass.toString().split(".").last().toUpperCase()
    protected var snackbar : Snackbar? = null

    private val connectivityManager: ConnectivityManager by lazy {
        requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    protected fun log(message : String) {
        Timber.d(TAG, message)
    }

    protected fun dismiss() {
        childFragmentManager.popBackStackImmediate()
    }

    protected fun transitionToFragment(fragment : androidx.fragment.app.Fragment, replace : Boolean = false) {
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