package com.relic.presentation.base

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.relic.dagger.RelicInjectable
import timber.log.Timber

open class RelicFragment: Fragment(), RelicInjectable {
    protected val TAG : String = javaClass.toString().split(".").last().toUpperCase()
    protected var snackbar : Snackbar? = null

    // avoid naming conflict with default activity getter
    protected val relicActivity: RelicActivity
        get() = requireActivity() as RelicActivity
    protected val supportFragmentManager : FragmentManager
        get() = requireActivity().supportFragmentManager


    private val connectivityManager: ConnectivityManager by lazy {
        requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    protected fun log(message : String) {
        Timber.d(TAG, message)
    }

    protected fun dismiss() {
        childFragmentManager.popBackStackImmediate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel(viewLifecycleOwner)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var override = true

        item.itemId.let { id ->
            when (id) {
                android.R.id.home -> activity?.onBackPressed()
                else -> override = super.onOptionsItemSelected(item)
            }
        }

        return override
    }

    protected open fun bindViewModel(lifecycleOwner : LifecycleOwner) { }

    /*
    handles behaviour when the current navigation item is pressed again
    ex. on "home" navigation item -> open post -> press home" navigation item again

    @return: whether the home pressed is handled by the fragment, otherwise will default to parent
     */
    open fun handleNavReselected(): Boolean { return false }

    open fun onBackPressed(): Boolean { return false }

    protected fun checkInternetConnectivity() : Boolean {
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}