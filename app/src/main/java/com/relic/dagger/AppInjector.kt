package com.relic.dagger

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.relic.RelicApp
import com.relic.dagger.modules.AppModule
import com.relic.dagger.modules.RepoModule
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector

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
     * register lifecycle callbaks for an app
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

        // need to also register callbacks for fragments
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(object :FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                    if (f is Injectable) {
                        AndroidSupportInjection.inject(f)
                    }
                }
            }, true)
        }
    }
}