package com.relic.presentation.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysubs.DisplaySubsView
import com.relic.presentation.home.frontpage.FrontpageFragment
import kotlinx.android.synthetic.main.home.view.*
import kotlinx.android.synthetic.main.relic_toolbar.view.*

class HomeFragment : RelicFragment() {

    private lateinit var pagerAdapter : HomePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pagerAdapter = HomePagerAdapter(childFragmentManager).apply {
            tabFragments.add(DisplaySubsView())
            tabFragments.add(FrontpageFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home, container, false).apply {
            homeViewPager.adapter = pagerAdapter
            homeTabLayout.setupWithViewPager(homeViewPager)

            homeToolbarView?.findViewById<TextView>(R.id.my_toolbar_title)?.text = resources.getString(R.string.app_name)
        }
    }

    private inner class HomePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        val tabFragmentTitles = listOf("HOME", "FRONTPAGE")
        val tabFragments = ArrayList<Fragment>()

        override fun getPageTitle(position: Int): CharSequence? {
            return tabFragmentTitles[position]
        }

        override fun getCount() = tabFragments.size

        override fun getItem(position: Int): Fragment {
            return tabFragments[position]
        }
    }
}