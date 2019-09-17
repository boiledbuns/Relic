package com.relic.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostVisitRelation (
    @PrimaryKey
    val visitedFullname : String
)