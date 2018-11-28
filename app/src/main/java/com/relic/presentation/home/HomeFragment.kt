package com.relic.presentation.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.R
import com.relic.presentation.displaysubs.DisplaySubsView
import kotlinx.android.synthetic.main.home.*

class HomeFragment : Fragment() {
    private val NUM_TABS = 1
    private val tabFragmentTitles = listOf("HOME", "FRONTPAGE")
    private val tabFragments = ArrayList<Fragment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)?.apply {
            val pagerAdapter = HomePagerAdapter(activity!!.supportFragmentManager)
            findViewById<ViewPager>(R.id.homePagerAdapter).adapter = pagerAdapter
        }
    }

    private fun initializeFragments() {
        tabFragments.add(DisplaySubsView())

    }

    private inner class HomePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getPageTitle(position: Int): CharSequence? {
            return tabFragmentTitles[position]
        }

        override fun getCount() = NUM_TABS

        override fun getItem(position: Int): Fragment {
            return tabFragments[position]
        }
    }
}