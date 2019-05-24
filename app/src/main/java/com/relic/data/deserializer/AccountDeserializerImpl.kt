package com.relic.data.deserializer

import android.util.Log
import com.google.gson.GsonBuilder
import com.relic.data.entities.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

class AccountDeserializerImpl : Contract.AccountDeserializer {
    private val TAG = "ACCOUNT_DESERIALIZER"

    private val gson = GsonBuilder().create()
    private val jsonParser: JSONParser = JSONParser()

    override suspend fun parseAccount(response: String): AccountEntity {
        val account = jsonParser.parse(response) as JSONObject

        Log.d(TAG, account.keys.toString())
        for (key in account.keys){
            Log.d(TAG, key.toString() + " " + account[key].toString())
        }

        return withContext(Dispatchers.Default) {
            try {
                gson.fromJson(response, AccountEntity::class.java)
            }
            catch (e : ParseException){
                throw RelicParseException(response, e)
            }
        }
    }

}