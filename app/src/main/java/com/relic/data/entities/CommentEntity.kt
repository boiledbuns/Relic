package com.relic.data.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class CommentEntity {

    @PrimaryKey
    var id: String = ""
    var fullName: String? = null
        get() = "t1_$id"

    //public String replies;
    var created: String? = null
    var author = ""
    var subreddit: String = ""

    // post parent id
    var parentPostId: String? = null

    // parent comment id
    @ColumnInfo(name = "parentFullname")
    var parent_id: String = ""

    @ColumnInfo(name = "body")
    var body_html: String? = null

    var score: Int = 0

    var platinum: Int = 0
    var gold: Int = 0
    var silver: Int = 0

    @ColumnInfo(name = "authorFlairText")
    var author_flair_text: String? = null
    @ColumnInfo(name = "authorFlairTextColor")
    var author_flair_text_color: String? = null

    @ColumnInfo(name = "isSubmitter")
    var submitter: Boolean = false
    @ColumnInfo(name = "scoreHidden")
    var score_hidden: Boolean = false
    var userUpvoted: Int = 0
    var saved: Boolean = false
    var visited: Boolean = false

    var replyCount: Int = 0
    var depth: Int = 0
    var replyLink: String? = null

    @ColumnInfo(name = "linkTitle")
    var link_title: String? = null
    @ColumnInfo(name = "linkAuthor")
    var link_author: String? = null

    var editedDate: String? = null

    var position: Float = 0.toFloat()

    // for "more" values
    var moreChildren: String? = null

    companion object {
        val MORE_CREATED = "more_author"
    }
}
