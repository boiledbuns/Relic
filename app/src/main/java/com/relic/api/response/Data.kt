package com.relic.api.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class Data <T> {
    var modhash : String? = null
    var dist : String? = null
    var children : List<T>? = null
    var after : String? = null
}
