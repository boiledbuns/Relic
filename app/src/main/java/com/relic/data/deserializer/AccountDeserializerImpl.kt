package com.relic.data.deserializer

import com.google.gson.GsonBuilder
import com.relic.persistence.entities.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountDeserializerImpl @Inject constructor() : Contract.AccountDeserializer {
    private val TAG = "ACCOUNT_DESERIALIZER"

    private val gson = GsonBuilder().create()
    private val jsonParser: JSONParser = JSONParser()

    override suspend fun parseAccount(response: String): AccountEntity {
        val account = jsonParser.parse(response) as JSONObject

        Timber.d(account.keys.toString())
        for (key in account.keys){
            Timber.d("$key ${account[key]}")
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