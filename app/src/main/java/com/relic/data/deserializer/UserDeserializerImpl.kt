package com.relic.data.deserializer

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.relic.data.models.UserModel
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

class UserDeserializerImpl(
    appContext : Context
) : Contract.UserDeserializer {

    private val TAG = "USER_DESERIALIZER"

    private val jsonParser: JSONParser = JSONParser()
    private val gson = GsonBuilder().create()

    override suspend fun parseUser(userResponse: String, trophiesResponse : String): UserModel {
        val userData = (jsonParser.parse(userResponse) as JSONObject)["data"] as JSONObject?

        val trophiesData = (jsonParser.parse(trophiesResponse) as JSONObject)["data"] as JSONObject?
        val trophies = trophiesData?.get("trophies") as JSONArray?

        userData?.apply {
            Log.d(TAG, "Keys : $keys")

            forEach { key ->
                Log.d(TAG, "$key\n")
            }
        }

        trophies?.apply {
            forEach { trophyJson ->
                val trophyData = (trophyJson as JSONObject)
                (trophyData["data"] as JSONObject).keys.forEach { key ->
                    Log.d(TAG, "$key\n")
                }
            }
        }

        return UserModel().apply {
            userData?.let {
                (it["name"] as String?)?.let { name = it }
                (it["gold_expiration"] as String?)?.let { goldExpiration = it }
                (it["icon_img"] as String?)?.let { iconImg = it }
                (it["link_karma"] as Long?)?.let { linkKarma = it.toInt() }

                (it["comment_karma"] as Long?)?.let { commentKarma = it.toInt() }
                isMod = (it["is_mod"] as Boolean?) ?: false

                (it["coins"] as Long?)?.let { coins = it.toInt() }
                (it["created"] as Double?)?.let { created = it.toString() }
            }
        }
    }

}