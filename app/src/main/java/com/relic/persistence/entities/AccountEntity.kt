package com.relic.persistence.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class AccountEntity {
    // Note: Doesn't include all settings, only the ones I feel are relevant rn
    // More settings might be added later

    @PrimaryKey
    var name : String = ""

    @ColumnInfo(name = "autoplayVideo")
    var video_autoplay : Boolean = false

    @ColumnInfo(name = "overAge")
    var over_18 : Boolean = false

    @ColumnInfo(name = "searchOverAge")
    var search_include_over_18 : Boolean = false

    @ColumnInfo(name = "defaultCommentSort")
    var default_comment_sort : String = ""

    @ColumnInfo(name = "minLinkScore")
    var min_link_score : Int = 0

    @ColumnInfo(name = "publicVotes")
    var public_votes : Boolean = false

    @ColumnInfo(name = "showFlair")
    var show_flair : Boolean = false

    @ColumnInfo(name = "showLinkFlair")
    var show_link_flair : Boolean = false

    @ColumnInfo(name = "nightmode")
    var nightmode : Boolean = false

    @ColumnInfo(name = "acceptPMs")
    var accept_pms : String = ""
}