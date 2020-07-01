package com.relic.presentation.util


object RedditTypeHelper {


    // removes the type associated with the comment, leaving only its id
    // ex. fullname = t3_15bfi0, id = 15bfi0
    fun getIdFromFullname(fullName : String) : String {
        return if (fullName.length >= 4) {
            fullName.removeRange(0, 3)
        } else {
            ""
        }
    }
}
