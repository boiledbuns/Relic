package com.relic.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Additional note: This class entity functions as a way to keep track of the most recent paging
 * "after" value to allow us to continue to load the next page based on Reddit's API structure
 */

@Entity
data class ListingEntity(
    @PrimaryKey
    val postSource: String,
    val after: String?
)