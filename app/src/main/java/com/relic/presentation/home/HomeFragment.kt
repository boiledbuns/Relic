package com.relic.presentation.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.*
import com.relic.presentation.main.MainActivity
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysubs.DisplaySubsView
import com.relic.presentation.home.frontpage.FrontpageFragment
import kotlinx.android.synthetic.main.home.view.*

class HomeFragment : RelicFragment() {

    private lateinit var pagerAdapter : HomePagerAdapter
    private lateinit var toggle : ActionBarDrawerToggle

    // region lifecycle hooks

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

            (homeToolbarView as Toolbar).let { toolbar ->
                (activity as MainActivity).setSupportActionBar(toolbar)
                initializeToolbar()
                toolbar.setNavigationOnClickListener {
                    (activity as MainActivity).getNavDrawer().openDrawer(Gravity.START)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // endregion lifecycle hooks

    private fun initializeToolbar() {
        val pActivity = (activity as MainActivity)

        toggle = ActionBarDrawerToggle(activity, pActivity.getNavDrawer(), R.string.app_name, R.string.app_name)
        pActivity.getNavDrawer().addDrawerListener(toggle)

        pActivity.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        toggle.syncState()
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