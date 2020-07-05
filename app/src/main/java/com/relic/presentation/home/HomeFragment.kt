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
import com.relic.data.PostSource
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.home.frontpage.MultiFragment
import com.relic.presentation.home.frontpage.MultiFragmentArgs
import kotlinx.android.synthetic.main.home.*

class HomeFragment : RelicFragment() {

    private lateinit var pagerAdapter: HomePagerAdapter
    private lateinit var toggle: ActionBarDrawerToggle

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pagerAdapter = HomePagerAdapter(childFragmentManager).apply {
            val frontpageFragment = MultiFragment().apply {
                arguments = MultiFragmentArgs(PostSource.Frontpage.getSourceName()).toBundle()
            }
            val allFragment = MultiFragment().apply {
                arguments = MultiFragmentArgs(PostSource.All.getSourceName()).toBundle()
            }
            tabFragments.add(frontpageFragment)
            tabFragments.add(allFragment)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewPager.adapter = pagerAdapter
        homeViewPager.offscreenPageLimit = 1
        homeTabLayout.setupWithViewPager(homeViewPager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun handleNavReselected(): Boolean {
        val activeFragment = pagerAdapter.getItem(homeViewPager.currentItem)

        (activeFragment as? RelicFragment?)?.let { relicFrag ->
            return relicFrag.handleNavReselected()
        }

        // always return true since this is a base fragment we don't want back press
        return true
    }

    // endregion lifecycle hooks

    private inner class HomePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        val tabFragmentTitles = listOf("FRONTPAGE", "ALL")
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