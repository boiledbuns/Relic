package com.relic.presentation.displayuser

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.relic.MainActivity
import com.relic.R
import com.relic.dagger.DaggerVMComponent
import com.relic.dagger.modules.AuthModule
import com.relic.dagger.modules.RepoModule
import com.relic.data.models.UserModel
import com.relic.presentation.base.RelicFragment
import com.relic.presentation.displayuser.fragments.PostsTabFragment
import com.shopify.livedataktx.nonNull
import com.shopify.livedataktx.observe
import kotlinx.android.synthetic.main.display_user.*
import kotlinx.android.synthetic.main.display_user.view.*

class DisplayUserFragment : RelicFragment() {

    private lateinit var displayUserVM : DisplayUserVM
    private lateinit var username : String

    private lateinit var pagerAdapter: UserContentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_USERNAME)?.let { username = it }

        displayUserVM = ViewModelProviders.of(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DaggerVMComponent.builder()
                    .repoModule(RepoModule(context!!))
                    .authModule(AuthModule(context!!))
                    .build()
                    .getDisplayUserVM().create(username) as T
            }
        }).get(DisplayUserVM::class.java)

        pagerAdapter = UserContentPagerAdapter(childFragmentManager).apply {
            contentFragments.add(PostsTabFragment.create(UserTab.Submitted))
            contentFragments.add(PostsTabFragment.create(UserTab.Comments))
            contentFragments.add(PostsTabFragment.create(UserTab.Saved))
            contentFragments.add(PostsTabFragment.create(UserTab.Upvoted))
            contentFragments.add(PostsTabFragment.create(UserTab.Downvoted))
            contentFragments.add(PostsTabFragment.create(UserTab.Gilded))
            contentFragments.add(PostsTabFragment.create(UserTab.Hidden))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.display_user, container, false).apply {
            (userToolbar as Toolbar).apply {
                title = getString(R.string.user_prefix_label, username)
                (activity as MainActivity).setSupportActionBar(this)
            }

            userViewPager.adapter = pagerAdapter
            userViewPager.offscreenPageLimit = 1
            userTabLayout.setupWithViewPager(userViewPager)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViewModel(viewLifecycleOwner)
        (userToolbar as Toolbar).setNavigationOnClickListener { activity?.onBackPressed() }
    }

    override fun bindViewModel(lifecycleOwner: LifecycleOwner) {
        displayUserVM.userLiveData.nonNull().observe (lifecycleOwner) { updateUserInfo(it) }
    }

    private fun updateUserInfo(userModel: UserModel) {
        linkKarma.text = userModel.linkKarma.toString()
        commentKarma.text = userModel.commentKarma.toString()

        totalKarma.text = (userModel.linkKarma + userModel.commentKarma).toString()

        val userAge = "test"
        val userCreationDate = "test"

        userCreated.text = resources.getString(R.string.account_age, userAge, userCreationDate)
    }

    // region livedata handlers

    // endregion livedata handlers

    companion object {
        val ARG_USERNAME = "arg_username"

        fun create(username : String) : DisplayUserFragment {
            val bundle = Bundle().apply {
                putString(ARG_USERNAME, username)
            }

            return DisplayUserFragment().apply {
                arguments = bundle
            }
        }
    }

    private inner class UserContentPagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {
        val contentFragmentTitles = listOf("Submissions", "Comments", "Saved", "Upvoted", "Downvoted", "Gilded", "Hidden")
        val contentFragments : ArrayList<Fragment> = ArrayList()

        override fun getItem(p0: Int): Fragment = contentFragments[p0]

        override fun getCount(): Int = contentFragments.size

        override fun getPageTitle(position: Int): CharSequence? {
            return contentFragmentTitles[position]
        }
    }
}