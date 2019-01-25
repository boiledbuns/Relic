package com.relic.data.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import com.relic.data.entities.PostSourceEntity

class ItemSourceModel {
    @Embedded
    var postSourceEntity : PostSourceEntity? = null

    @Relation(entityColumn = "sourceId", parentColumn = "id")
    var postModel : PostModel? = null

    @Relation(entityColumn = "sourceId", parentColumn = "id")
    var commentModel : CommentModel? = null
}