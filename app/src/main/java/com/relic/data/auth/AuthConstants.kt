package com.relic.data.auth

internal object AuthConstants {
    const val BASE = "https://www.reddit.com/api/v1/authorize.compact?"
    const val ACCESS_TOKEN_URI = "https://www.reddit.com/api/v1/access_token"
    const val REDIRECT = "https://github.com/13ABEL/Relic"
    const val DURATION = "permanent"

    const val RESPONSE_TYPE = "code"
    const val STATE = "random0101" // any random value
    const val SCOPE = "identity account mysubreddits edit flair history modconfig modflair modlog modposts modwiki" +
        " mysubreddits privatemessages read report save submit subscribe vote wikiedit wikiread"

    // keys to fields in authentication response json
    const val ATOKEN_KEY = "access_token"
    const val RTOKEN_KEY = "refresh_token"
}