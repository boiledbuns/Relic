package com.relic.presentation.base

import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.relic.R
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class RelicActivity : AppCompatActivity(), HasSupportFragmentInjector {
    protected val TAG : String = javaClass.toString().split(".").last().toUpperCase()

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

    protected fun transitionToFragment(fragment : Fragment, replace : Boolean = false) {
        supportFragmentManager
          .beginTransaction()
          .replace(R.id.main_content_frame, fragment)
          .addToBackStack(TAG)
          .commit()
    }
}