package com.relic.domain.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Award(
    val subreddit_id: String?,
    val is_new: Boolean,
    val coin_price: Int,
    val id: String,
    val coin_reward: Int,
    val icon_url: String,
    val days_of_premium: Int,
    val icon_width: Int,
    val static_icon_width: Int,
    val is_enabled: Boolean,
    val description: String,
    val subreddit_coin_reward: Int,
    val count: Int,
    val static_icon_height: Int,
    val name: String,
    val static_icon_url: String
)

/* additional fields not currently used
"resized_static_icons": [],
"days_of_drip_extension":0,
"penny_donate":null,
"award_sub_type":"PREMIUM",
"resized_icons":[],
"start_date":null,
"end_date":null,
"icon_format":null,
"icon_height":2048,
"penny_price":null,
"award_type":"global",
 */