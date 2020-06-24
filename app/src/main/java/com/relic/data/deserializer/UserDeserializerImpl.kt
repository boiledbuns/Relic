package com.relic.data.deserializer

import com.relic.api.response.Listing
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.domain.models.UserModel
import com.relic.persistence.RoomTypeConverters.Companion.moshi
import com.squareup.moshi.Types
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import timber.log.Timber
import javax.inject.Inject

class UserDeserializerImpl @Inject constructor(): Contract.UserDeserializer {

    private val userType = Types.newParameterizedType(Listing::class.java, ListingItem::class.java)
    private val userAdapter = moshi.adapter(UserModel::class.java)
    private val userListingAdapter = moshi.adapter<Listing<UserModel>>(userType)

    private val jsonParser: JSONParser = JSONParser()

    override suspend fun parseUser(userResponse: String, trophiesResponse : String): UserModel {
        val userData = (jsonParser.parse(userResponse) as JSONObject)["data"] as JSONObject
        val trophiesData = (jsonParser.parse(trophiesResponse) as JSONObject)["data"] as JSONObject

        try {
            val trophies = trophiesData.get("trophies") as JSONArray?

            trophies?.apply {
                forEach { trophyJson ->
                    val trophyData = (trophyJson as JSONObject)
                    (trophyData["data"] as JSONObject).keys.forEach { key ->
                        Timber.d("$key\n")
                    }
                }
            }
        } catch (e : ParseException) {
            throw RelicParseException("error parsing user trophies", e)
        }

        try {
            return userAdapter.fromJson(userResponse)!!
        } catch (e : ParseException) {
            throw RelicParseException("error parsing user", e)
        }
    }

    override suspend fun parseUsers(response: String): Listing<UserModel> {
        try {
            return userListingAdapter.fromJson(response)!!
        } catch (e : ParseException){
            throw RelicParseException(response, e)
        }
    }

    override suspend fun parseUsername(response: String): String {
        val responseJson = jsonParser.parse(response) as JSONObject
        return responseJson["name"] as String
    }
}