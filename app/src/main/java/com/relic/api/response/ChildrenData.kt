package com.relic.api.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ChildrenData<T> {
    var data : T? = null
}