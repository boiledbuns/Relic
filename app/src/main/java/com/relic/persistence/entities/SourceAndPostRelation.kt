package com.relic.persistence.entities

import android.arch.persistence.room.Entity

/*
Relation for a "Source" and a "Post"
 */
@Entity(primaryKeys = ["source", "postId"])
data class SourceAndPostRelation(
    val source : String,
    val postId: String,
    // extra field for this position - denotes position of a post within the source
    val position : Int
)