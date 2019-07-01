package com.relic.data.deserializer

import com.google.gson.GsonBuilder
import com.relic.api.response.Listing
import com.relic.domain.models.SubredditModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import java.util.*
import javax.inject.Inject

class SubDeserializerImpl @Inject constructor(
    private val moshi : Moshi
): Contract.SubDeserializer {
    private val TAG = "SUB_DESERIALIZER"


    private val subType = Types.newParameterizedType(Listing::class.java, SubredditModel::class.java)
    private val subListingAdapter = moshi.adapter<Listing<SubredditModel>>(subType)

    private val parser: JSONParser = JSONParser()
    private val gson = GsonBuilder().create()

    override suspend fun parseSubredditResponse(response: String): SubredditModel {
        // parse the response and add it to an arraylist to be inserted in the db
        val subredditObject = (parser.parse(response) as JSONObject)["data"] as JSONObject?
        return gson.fromJson(subredditObject!!.toJSONString(), SubredditModel::class.java)
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
    override suspend fun parseSubredditsResponse(response: String): Listing<SubredditModel> {
        try {
            return subListingAdapter.fromJson(response)!!
        } catch (e : ParseException) {
            throw RelicParseException(response, e)
        }
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