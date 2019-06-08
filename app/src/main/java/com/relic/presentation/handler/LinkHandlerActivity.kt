package com.relic.presentation.handler

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.relic.R
import com.relic.data.PostRepository
import com.relic.presentation.base.RelicActivity
import com.relic.presentation.displaypost.DisplayPostFragment
import com.relic.presentation.displaysub.DisplaySubFragment
import com.relic.presentation.displayuser.DisplayUserFragment

class LinkHandlerActivity : RelicActivity() {
    internal val TAG = "LINK_HANDLER_ACTIVITY"

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
            Log.d(TAG, urlDetails.toString())
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
                                postSource = PostRepository.PostSource.Subreddit(urlDetails[1]),
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