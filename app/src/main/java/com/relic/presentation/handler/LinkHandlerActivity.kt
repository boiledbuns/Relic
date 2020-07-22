package com.relic.presentation.handler

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.relic.R
import com.relic.presentation.base.RelicActivity
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaypost.DisplayPostFragmentArgs
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.displaysub.DisplaySubFragmentArgs
import com.relic.presentation.displayuser.DisplayUserFragment
import com.relic.presentation.displayuser.DisplayUserFragmentArgs
import com.relic.presentation.home.frontpage.MultiFragmentArgs
import com.relic.presentation.main.PlaceholderFragment
import timber.log.Timber
import java.util.*

class LinkHandlerActivity : RelicActivity() {

    lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_link_handler)
        // set up nav fragment
        navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
        supportFragmentManager.beginTransaction()
            .add(R.id.link_handler_content_frame, navHostFragment)
            .commitNow()

        intent.action?.let { action ->
            if (action == Intent.ACTION_VIEW) {
                handleIntent(intent)
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        val path = intent.data?.path
        if (path.isNullOrEmpty()) {
            val args = MultiFragmentArgs(multiName = "frontpage").toBundle()
            navHostFragment.navController.navigate(R.id.multiFragment, args)
        } else {
            val tokens = path.split("/")
            val urlDetails = tokens.subList(1, tokens.size - 1)

            // first index determines the view we're going to (ie. sub or user")
            when (urlDetails[0]) {
                "r" -> {
                    // if just subreddit -> visit sub
                    if (urlDetails.size == 2) {
                        // don't navigate to specific multi reddits
                        if (urlDetails[1].toLowerCase(Locale.getDefault()) in listOf("all", "frontpage")) {
                            val args = MultiFragmentArgs(multiName = urlDetails[1]).toBundle()
                            navHostFragment.navController.navigate(R.id.multiFragment, args)
                        } else {
                            val args = DisplaySubFragmentArgs(subName = urlDetails[1]).toBundle()
                            navHostFragment.navController.navigate(R.id.displaySubFragment, args)
                        }
                    } else if (urlDetails.size == 5) {
                        // if specific post -> visit post
                        val args = DisplayPostFragmentArgs(
                            postFullName = "t3_" + urlDetails[3],
                            enableVisitSub = false,
                            subredditName = urlDetails[1]
                        ).toBundle()
                        navHostFragment.navController.navigate(R.id.displayPostFragment, args)
                    } else {
                        Timber.d("Unhandled subreddit $path")
                    }
                }
                "u" -> {
                    val args = DisplayUserFragmentArgs(username = urlDetails[1]).toBundle()
                    navHostFragment.navController.navigate(R.id.displayUserFragment, args)
                }
                else -> {
                    Timber.d("Unhandled link $path")
                }
            }
        }
    }
}