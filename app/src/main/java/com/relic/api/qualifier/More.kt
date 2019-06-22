package com.relic.api.qualifier

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

@JsonQualifier
internal annotation class More

class MoreAdapter {

    @FromJson
    @More
    fun fromJson(reader: JsonReader) : List<String>? {
        return if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull()
        } else {
            ArrayList<String>().apply {

                reader.beginArray()
                while (reader.hasNext()) {
                    add(reader.nextString())
                }
                reader.endArray()
            }
        }
    }

    @ToJson
    fun toJson(@More more : List<String>) : String? {
        return ""
    }
}