package com.relic.data.entities

import android.arch.persistence.room.Entity

@Entity
class PostSourceEntity {

    // since a post can  only appear in any combination of the three sources
    var subredditPosition : Int = -1
    var frontpagePosition : Int  = -1
    var allPosition : Int = -1
}