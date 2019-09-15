package com.relic.data.deserializer

import com.relic.domain.models.UserModel
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import timber.log.Timber
import javax.inject.Inject

class UserDeserializerImpl @Inject constructor(): Contract.UserDeserializer {
    private val TAG = "USER_DESERIALIZER"

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

        Timber.d("Keys : ${userData.keys}")
        userData.forEach { key ->
            Timber.d("$key\n")
        }

        return UserModel().apply {
            try {
                userData.let {
                    (it["name"] as String?)?.let { name = it }
                    (it["gold_expiration"] as String?)?.let { goldExpiration = it }
                    (it["icon_img"] as String?)?.let { iconImg = it }
                    (it["link_karma"] as Long?)?.let { linkKarma = it.toInt() }

                    (it["comment_karma"] as Long?)?.let { commentKarma = it.toInt() }
                    isMod = (it["is_mod"] as Boolean?) ?: false

                    (it["coins"] as Long?)?.let { coins = it.toInt() }
                    (it["created"] as Double?)?.let { created = it.toString() }
                }
            } catch (e : ParseException) {
                throw RelicParseException("error parsing user", e)
            }
        }
    }

    override suspend fun parseUsers(usersResponse: String): List<UserModel> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun parseUsername(response: String): String {
        val responseJson = jsonParser.parse(response) as JSONObject
        return responseJson["name"] as String
    }
}