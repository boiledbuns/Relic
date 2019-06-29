package com.relic.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class PostVisitRelation (
    @PrimaryKey
    val visitedFullname : String
)