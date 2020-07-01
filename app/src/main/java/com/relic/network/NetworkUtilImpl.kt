package com.relic.network

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkInfo
import javax.inject.Inject

class NetworkUtilImpl @Inject constructor(
    // using application context won't create a memory leak
    private val appContext : Application
) : NetworkUtil {

    override fun checkConnection () : Boolean {
        val cm = appContext.getSystemService(Application.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo : NetworkInfo? = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}