package com.relic.data.deserializer

import org.json.simple.JSONArray
import org.json.simple.JSONObject

fun JSONObject.unwrapListing() : JSONObject {
    return get("data") as JSONObject
}

fun JSONObject.unwrapChild() : JSONObject {
    return get("data") as JSONObject
}

fun JSONObject.children() : JSONArray {
    return get("children") as JSONArray
}