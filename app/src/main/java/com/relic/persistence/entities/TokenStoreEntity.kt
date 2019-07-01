package com.relic.persistence.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class TokenStoreEntity {
    @PrimaryKey
    var accountName : String = ""

    var refresh : String = ""
    var access : String = ""
}