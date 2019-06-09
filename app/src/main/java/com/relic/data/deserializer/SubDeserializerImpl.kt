package com.relic.data.deserializer

import com.google.gson.GsonBuilder
import com.relic.data.entities.SubredditEntity
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.util.ArrayList
import javax.inject.Inject

class SubDeserializerImpl @Inject constructor(): Contract.SubDeserializer {
    private val TAG = "SUB_DESERIALIZER"

    private val parser: JSONParser = JSONParser()
    private val gson = GsonBuilder().create()

    override suspend fun parseSubredditResponse(response: String): SubredditEntity {
        // parse the response and add it to an arraylist to be inserted in the db
        val subredditObject = (parser.parse(response) as JSONObject)["data"] as JSONObject?
        return gson.fromJson(subredditObject!!.toJSONString(), SubredditEntity::class.java)
    }

    /**
     * Parses a "listing" for subreddits into a list of Subreddit
     * @param response JSON formatted listing for the subreddits to be parsed
     * @return list of subreddit entities parsed from the string
     * @throws ParseException potential GSON exception when unmarshalling object
     */
    // TODO convert error to deserializer error -> classes using it should not be aware of what
    // this class uses to parse response
    @Throws(RelicParseException::class)
    override suspend fun parseSubredditsResponse(response: String): ParsedSubsData {
        val data = (parser.parse(response) as JSONObject)["data"] as JSONObject
        val newAfter = data["after"] as String?

        val subscribed = ArrayList<SubredditEntity>()

        try {
            // get all the subs that the user is subscribed to
            val subs = data["children"] as JSONArray?
            for (sub in subs!!) {
                val currentSub = (sub as JSONObject)["data"] as JSONObject
                // Log.d(TAG, "keys = " + currentSub.keySet());
                // Log.d(TAG, "banner url  = " + currentSub.get("banner_background_image") + " " + currentSub.get("banner_img"));
                // Log.d(TAG, currentSub.get("display_name") + "banner url  = " + currentSub.get("community_icon") + " " + currentSub.get("icon_img"));
                subscribed.add(gson.fromJson(currentSub.toJSONString(), SubredditEntity::class.java))
            }
        } catch (e : ParseException) {
            throw RelicParseException(response, e)
        }

        //Log.d(TAG, subscribed.toString());
        return ParsedSubsData(
            subsList = subscribed,
            after = newAfter
        )
    }


    /**
     * Parses the api response to obtain the list of subreddit names
     * @param response search_subreddits api response
     */
    override suspend fun parseSearchSubsResponse(response: String): List<String> {
        val parsedMatches = ArrayList<String>()

        try {
            val subreddits = (parser.parse(response) as JSONObject)["subreddits"] as JSONArray?
            val subIterator = subreddits!!.iterator()

            while (subIterator.hasNext()) {
                parsedMatches.add((subIterator.next() as JSONObject)["name"] as String)
            }
        } catch (e: ParseException) {
            throw RelicParseException(response, e)
        }

        return parsedMatches
    }
}