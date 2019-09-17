package com.relic.presentation.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import android.view.*
import androidx.core.view.GravityCompat
import com.relic.presentation.main.MainActivity
import com.relic.R
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displaysubs.DisplaySubsFragment
import com.relic.presentation.home.frontpage.FrontpageFragment
import kotlinx.android.synthetic.main.home.view.*

class HomeFragment : RelicFragment() {

    private lateinit var pagerAdapter : HomePagerAdapter
    private lateinit var toggle : ActionBarDrawerToggle

    // region lifecycle hooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pagerAdapter = HomePagerAdapter(childFragmentManager).apply {
            tabFragments.add(DisplaySubsFragment())
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
                    (activity as MainActivity).getNavDrawer().openDrawer(GravityCompat.START)
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

    private inner class HomePagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {
        val tabFragmentTitles = listOf("HOME", "FRONTPAGE")
        val tabFragments = ArrayList<androidx.fragment.app.Fragment>()

        override fun getPageTitle(position: Int): CharSequence? {
            return tabFragmentTitles[position]
        }

        override fun getCount() = tabFragments.size

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            return tabFragments[position]
        }
    }
}