package com.relic.data.deserializer

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.relic.data.entities.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

class AccountDeserializerImpl(
    appContext : Context
) : Contract.AccountDeserializer {
    private val TAG = "ACCOUNT_DESERIALIZER"

    private val gson = GsonBuilder().create()
    private val jsonParser: JSONParser = JSONParser()

    override suspend fun parseAccount(accountResponse: String): AccountEntity {
        val account = jsonParser.parse(accountResponse) as JSONObject
        Log.d(TAG, accountResponse)
        Log.d(TAG, account.keys.toString())
        for (key in account.keys){
            Log.d(TAG, key.toString() + " " + account[key].toString())
        }
        return withContext(Dispatchers.Default) {
            try {
                gson.fromJson(accountResponse, AccountEntity::class.java)
            }
            catch (e : JsonSyntaxException){
                throw DeserializationException("error parsing account", e)
            }
        }
    }

}