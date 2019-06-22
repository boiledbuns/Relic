package com.relic.api.qualifier

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.util.*

@JsonQualifier
internal annotation class Date

class DateAdapter {

    @FromJson
    @Date
    fun fromJson(reader: JsonReader) : java.util.Date? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> { reader.nextNull() }
            JsonReader.Token.BOOLEAN -> {
                // only used for "edited" field since it's set as "false" if not edited
                reader.nextBoolean()
                null
            }
            else -> {
                Date(reader.nextLong() * 1000)
            }
        }
    }

    @ToJson
    fun toJson(@Date edited : java.util.Date?) : String? {
        return null
    }
}