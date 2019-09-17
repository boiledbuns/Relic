package com.relic.dagger

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.relic.RelicApp
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector

/**
 * This object basically controls how injections are handled for all activities & fragments
 * in the app. It hooks onto lifecycle callbacks to perform injections.
 */
object AppInjector {
    lateinit var appComponent : AppComponent

    fun init(relic : RelicApp)  {
        appComponent = DaggerAppComponent
            .builder()
            .application(relic)
            .context(relic)
            .build()
        appComponent.inject(relic)

        registerActivityLifeCycleCallbacks(relic)
    }

    /**
     * register lifecycle callbacks for an activity
     * whenever there occur, our callbacks are triggered
     */
    private fun registerActivityLifeCycleCallbacks(relic : RelicApp) {
        relic.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                activity?.let {
                    handleInjection(it)
                }
            }

            // we don't need to do anything for any of these events
            override fun onActivityPaused(activity: Activity?) {}
            override fun onActivityResumed(activity: Activity?) {}
            override fun onActivityStarted(activity: Activity?) {}
            override fun onActivityDestroyed(activity: Activity?) {}
            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
            override fun onActivityStopped(activity: Activity?) {}
        })
    }

    private fun handleInjection(activity : Activity) {
        if (activity is HasSupportFragmentInjector) {
            AndroidInjection.inject(activity)
        }

        // need to also register callbacks for an injectable fragment
        if (activity is androidx.fragment.app.FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(object : androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentCreated(fm: androidx.fragment.app.FragmentManager, f: androidx.fragment.app.Fragment, savedInstanceState: Bundle?) {
                    if (f is RelicInjectable) {
                        AndroidSupportInjection.inject(f)
                    }
                }
            }, true)
        }
    }
}