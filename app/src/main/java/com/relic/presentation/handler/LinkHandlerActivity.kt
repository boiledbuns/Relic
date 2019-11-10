package com.relic.presentation.handler

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.relic.R
import com.relic.presentation.base.RelicActivity
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.displayuser.DisplayUserFragment
import timber.log.Timber

class LinkHandlerActivity : RelicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_link_handler)

        intent.action?.let { action ->
            if (action == Intent.ACTION_VIEW) {
                handleIntent(intent)
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        intent.data?.path?.let { path ->
            val tokens = path.split("/")
            val urlDetails = tokens.subList(1, tokens.size - 1)
            // not first index is an empty string
            Timber.d(urlDetails.toString())
            // first index determines the view we're going to (ie. sub or user")
            when {
                urlDetails[0] == "r" -> {
                    // if just subreddit -> visit sub
                    if (urlDetails.size == 2) {
                        displayFragment(DisplaySubFragment.create(urlDetails[1]))
                    }
                    // if specific post -> visit post
                    else if (urlDetails.size == 5) {
                        displayFragment(
                            DisplayPostFragment.create(
                                postId = "t3_" + urlDetails[3],
                                enableVisitSub = true,
                                subreddit = urlDetails[1]
                            )
                        )
                    }
                    else {

                    }
                }
                urlDetails[0] == "u" -> {
                    displayFragment(DisplayUserFragment.create(urlDetails[1]))
                }
                else -> {}
            }
        }
    }

    private fun displayFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.link_handler_content_frame, fragment)
            .commit()
    }
}