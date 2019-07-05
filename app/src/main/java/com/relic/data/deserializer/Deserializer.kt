package com.relic.data.deserializer

import com.relic.api.Type
import com.relic.api.adapter.CommentAdapter
import com.relic.api.adapter.PostAdapter
import com.relic.api.adapter.SubAdapter
import com.relic.api.qualifier.DateAdapter
import com.relic.api.qualifier.LikesAdapter
import com.relic.api.qualifier.MoreAdapter
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory

class Deserializer {

    companion object {
        private var INSTANCE : Moshi? = null

        fun getInstance() : Moshi {
            if (INSTANCE == null) {
                INSTANCE = Moshi.Builder()
                    .add(LikesAdapter())
                    .add(MoreAdapter())
                    .add(DateAdapter())
                    .add(
                        PolymorphicJsonAdapterFactory.of(ListingItem::class.java, "kind")
                            .withSubtype(PostModel::class.java, Type.Post.name)
                            .withSubtype(CommentModel::class.java, Type.Comment.name)
                    )
                    .add(PostAdapter())
                    .add(CommentAdapter())
                    .add(SubAdapter())
                    .build()
            }

            return INSTANCE!!
        }
    }
}