package com.relic.data.deserializer

import com.relic.api.response.Listing
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import javax.inject.Inject

class SubDeserializerImpl @Inject constructor(
    moshi : Moshi
): Contract.SubDeserializer {

    private val subAdapter = moshi.adapter(SubredditModel::class.java)
    private val subSearchAdapter = moshi.adapter(SubSearchResult::class.java)

    private val subType = Types.newParameterizedType(Listing::class.java, SubredditModel::class.java)
    private val subListingAdapter = moshi.adapter<Listing<SubredditModel>>(subType)

    override suspend fun parseSubredditResponse(response: String): SubredditModel {
        return subAdapter.fromJson(response)!!
    }

    override suspend fun parseSubredditsResponse(response: String): Listing<SubredditModel> {
        try {
            return subListingAdapter.fromJson(response)!!
        } catch (e : ParseException) {
            throw RelicParseException(response, e)
        }
    }

    override suspend fun parseSearchSubsResponse(response: String): List<SubPreviewModel> {
        try {
            val subSearchResult = subSearchAdapter.fromJson(response)
            return subSearchResult!!.subreddits
        } catch (e: ParseException) {
            throw RelicParseException(response, e)
        }
    }

    @JsonClass(generateAdapter = true)
    data class SubSearchResult(
        val subreddits : List<SubPreviewModel>
    )
}