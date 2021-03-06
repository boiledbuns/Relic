package com.relic.data.gateway

import android.text.Html
import com.relic.data.DomainTransfer
import com.relic.network.NetworkRequestManager
import com.relic.network.request.RelicOAuthRequest
import com.relic.persistence.ApplicationDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import timber.log.Timber
import javax.inject.Inject

class SubGatewayImpl @Inject constructor(
    private val appDB : ApplicationDB,
    private val requestManager: NetworkRequestManager
) : SubGateway {
    var TAG = "SUB_GATEWAY"
    private val ENDPOINT = "https://oauth.reddit.com/"
    private val NON_OAUTH_ENDPOINT = "https://www.reddit.com/"

    val GET_SUBINFO = 1
    val SUBSCRIBE = 2
    val UNSUBSCRIBE = 3

    private val subDAO = appDB.subredditDao

    // TODO change to "retrieve"
    override suspend fun retrieveAdditionalSubInfo(subredditName: String): String {
        // get sub info
        val end = ENDPOINT + "r/" + subredditName + "/about"
        Timber.d("from $end")

        return try {
            // TODO move this into deserializer
            val response = requestManager.processRequest(RelicOAuthRequest.GET, end)
            val parser = JSONParser()
            Timber.d(response)
            val subInfoObject = (parser.parse(response) as JSONObject)["data"] as JSONObject?
            Timber.d(subInfoObject!!.keys.toString())

            // user_is_moderator, header_title, subreddit_type, submit_text, display_name, accounts_active, submit_text_html, description_html
            // user_has_favorited, user_is_contributor, user_is_moderator, public_description, active_user_count, user_is_banned
            // public_traffic

            val info = subInfoObject["header_title"].toString() +
                "---- accounts active " + subInfoObject["active_user_count"]!!.toString() +
                "---- description html " + Html.fromHtml(Html.fromHtml(subInfoObject["description_html"] as String?).toString()) +
                "---- submit text " + subInfoObject["submit_text"]
            Timber.d(info)

            info.apply {
                withContext(Dispatchers.IO) {
                    subDAO.updateSubInfo(
                        subredditName,
                        subInfoObject["header_title"] as String?,
                        Html.escapeHtml(subInfoObject["description_html"] as String),
                        subInfoObject["submit_text"] as String?
                    )
                }
            }
        } catch (e : Exception) {
            throw DomainTransfer.handleException("retrieve additional sub info", e) ?: e
        }
    }

    override suspend fun retrieveSidebar(subredditName: String): String {
        // get sub sidebar
        val end = ENDPOINT + "r/" + subredditName + "about/sidebar"
        Timber.d("from $end")

        return try {
            requestManager.processRequest(RelicOAuthRequest.GET, end)
        }  catch (e : Exception) {
            throw DomainTransfer.handleException("retrieve sidebar", e) ?: e
        }
    }

    override suspend fun getIsSubscribed(subredditName: String): Boolean {
        return withContext(Dispatchers.IO) {
            subDAO.getSubscribed(subredditName) == null
        }
    }

    override suspend fun subscribe(isSubbed : Boolean, subName: String) {
        val action = if (isSubbed) "unsub" else "sub"
        val end = ENDPOINT + "api/subscribe?action=$action&sr_name=" + subName
        Timber.d("Subscribing to $end")

        try {
            requestManager.processRequest(RelicOAuthRequest.POST, end)
            Timber.d("Subscribed to $subName")

            withContext(Dispatchers.IO) {
                // update local entity to reflect the changes once successfully subscribed
                subDAO.updateSubscription(true, subName)
            }
        } catch (e : Exception) {
            throw DomainTransfer.handleException("subscribe", e) ?: e
        }
    }

    override suspend fun retrieveSubBanner(subName: String) {
        val end = ENDPOINT + "r/" + subName + "/stylesheet.css"

        try {
            var response = requestManager.processRequest(RelicOAuthRequest.GET, end)
            Timber.d("subname css : $response")

            val position = response.indexOf("#header")
            response = response.substring(position)

            // jump to the position of the css property for the banner image
            val backgroundProp = "background-image:url("
            var bannerUrlPosition = response.indexOf(backgroundProp) + backgroundProp.length + 1

            // proceed if a background image was found at all
            if (bannerUrlPosition == backgroundProp.length + 1) {
                Timber.d(" position of banner URL $bannerUrlPosition")

                var complete = false
                val stringBuilder = StringBuilder()
                // iterate through the response from that position until the full banner image url is parsed
                while (!complete) {
                    val charAtPosition = response[bannerUrlPosition]
                    // set loop flag to false if the end of the url is found
                    if (charAtPosition == '"') {
                        complete = true
                    } else {
                        stringBuilder.append(charAtPosition)
                        bannerUrlPosition++
                    }
                }
                Timber.d(" banner url = $stringBuilder")
            }
        } catch (e : Exception) {
            throw DomainTransfer.handleException("retrieve sub banner", e) ?: e
        }
    }

    /**
     * Parse response from subreddit into a string
     * @param response JSON representation of the subreddit information
     * @return subreddit information
     */
    private fun parseSubredditInfo(response: String): String {
        var info = response
        val parser = JSONParser()

        try {
            Timber.d(response)
            val subInfoObject = (parser.parse(response) as JSONObject)["data"] as JSONObject?
            Timber.d(subInfoObject!!.keys.toString())

            // user_is_moderator, header_title, subreddit_type, submit_text, display_name, accounts_active, submit_text_html, description_html
            // user_has_favorited, user_is_contributor, user_is_moderator, public_description, active_user_count, user_is_banned
            // public_traffic

            info = subInfoObject["public_description"].toString() +
                subInfoObject["accounts_active"]!!.toString() +
                Html.fromHtml(Html.fromHtml(subInfoObject["description_html"] as String?).toString()) +
                subInfoObject["description_html"] as String?

        } catch (e: ParseException) {
            Timber.d("Error parsing the response")
            info = "Error parsing the response"
        }

        return info
    }
}
