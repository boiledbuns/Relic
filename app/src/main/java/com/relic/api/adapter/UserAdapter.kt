package com.relic.api.adapter

import com.relic.domain.models.UserModel
import com.squareup.moshi.*

class UserAdapter {
    private val options: JsonReader.Options = JsonReader.Options.of("kind", "data")

    @FromJson
    fun fromJson(reader: JsonReader, delegate: JsonAdapter<UserModel>) : UserModel? {
        var user: UserModel? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                // we only want info nested inside the "data" object
                0 -> reader.skipValue()
                1 -> user = delegate.fromJson(reader)
                else -> {
                    // skip everything else
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        return user
    }

    @Suppress("unused")
    @ToJson
    fun toJson(writer : JsonWriter, value : UserModel) {}
}