package com.relic

interface MainContract {
    interface VM {
        /**
         * call this whenever the user account is changed (two scenarios)
         * 1. user logs in for the first time
         * 2. user is already logged in, but selects another account
         */
        fun onUserSelected()
    }
}