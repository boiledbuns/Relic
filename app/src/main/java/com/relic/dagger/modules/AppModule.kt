package com.relic.dagger.modules

import android.app.Application
import android.arch.persistence.room.Room
import com.relic.api.Type
import com.relic.api.adapter.CommentAdapter
import com.relic.api.adapter.PostAdapter
import com.relic.api.qualifier.DateAdapter
import com.relic.api.qualifier.LikesAdapter
import com.relic.api.qualifier.MoreAdapter
import com.relic.data.ApplicationDB
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideDB(app: Application) : ApplicationDB {
        return Room.databaseBuilder(app, ApplicationDB::class.java, "relic.db").build()
    }

    @Singleton
    @Provides
    fun provideMoshi() : Moshi {
        return Moshi.Builder()
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
            .build()
    }

}