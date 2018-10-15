package com.relic.presentation.subinfodialog

interface  SubInfoDialogContract {

    companion object {
        const val ARG_SUB_NAME = "subreddit_name"
    }

    interface Delegate {
        fun updateSubscriptionStatus(newStatus : Boolean)
        fun updatePinnedStatus(newStatus : Boolean)
    }
}