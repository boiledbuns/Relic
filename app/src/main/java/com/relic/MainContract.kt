package com.relic

interface MainContract {
    interface VM {
        /**
         * call this whenever the user account is changed (two scenarios)
         * 1. user logs in for the first time -> in this scenario, the name should be null because
         * retrieving and setting the current account should already have been handled
         * 2. user is already logged in, but selects another account -> in this scenario, the
         * name of the selected account needs to be presented to allow the vm to retrieve its details
         */
        fun onAccountSelected(name : String? = null)
    }
}