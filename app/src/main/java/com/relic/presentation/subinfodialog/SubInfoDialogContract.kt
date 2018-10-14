package com.relic.presentation.subinfodialog

interface  SubInfoDialogContract {
    interface Delegate {
        fun updateSubscriptionStatus(newStatus : Boolean)
        fun updatePinnedStatus(newStatus : Boolean)
    }
}