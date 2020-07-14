package com.relic.api.qualifier

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import org.apache.commons.text.StringEscapeUtils

/**
 * indicates a field is html encoded (ex. &gt; instead of >)
 * and will need to be converted
 */
@JsonQualifier
internal annotation class ConvertHtml

class HtmlTextAdapter {

    @FromJson
    @ConvertHtml
    fun fromJson(reader: JsonReader) : String? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> { reader.nextNull() }
            JsonReader.Token.STRING -> {
                val htmlText = reader.nextString()
                StringEscapeUtils.unescapeHtml4(htmlText)
            }
            else -> null
        }
    }

    @ToJson
    fun toJson(@ConvertHtml text : String?) : String? {
        return null
    }
}