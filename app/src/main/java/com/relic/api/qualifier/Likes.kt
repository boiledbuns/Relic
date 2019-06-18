package com.relic.api.qualifier

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

@JsonQualifier
internal annotation class Likes

class LikesAdapter {

    @FromJson @Likes
    fun fromJson(reader: JsonReader) : Int {
        val vote: Boolean? = if (reader.peek() === JsonReader.Token.NULL) {
            reader.nextNull()
        } else {
            reader.nextBoolean()
        }

        return when(vote) {
            null -> 0
            true -> 1
            false -> -1
        }
    }

    @ToJson
    fun toJson(@Likes vote : Int) : String? {
        return when(vote) {
            1 -> "true"
            -1 -> "false"
            else -> null
        }
    }
}