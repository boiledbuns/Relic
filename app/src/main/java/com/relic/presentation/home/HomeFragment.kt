package com.relic.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.home.frontpage.FrontpageFragment
import kotlinx.android.synthetic.main.home.view.*

class HomeFragment : RelicFragment() {

    private lateinit var pagerAdapter: HomePagerAdapter
    private lateinit var toggle: ActionBarDrawerToggle

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pagerAdapter = HomePagerAdapter(childFragmentManager).apply {
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
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // endregion lifecycle hooks

    private inner class HomePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        val tabFragmentTitles = listOf("FRONTPAGE")
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