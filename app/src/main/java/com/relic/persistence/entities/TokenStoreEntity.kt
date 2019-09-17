package com.relic.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TokenStoreEntity {
    @PrimaryKey
    var accountName : String = ""

    var refresh : String = ""
    var access : String = ""
}