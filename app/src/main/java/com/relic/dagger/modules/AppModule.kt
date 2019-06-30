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
import com.relic.data.TypeConverters
import com.relic.data.deserializer.Deserializer
import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.domain.models.PostModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import dagger.Module
import dagger.Provides
import org.json.simple.parser.JSONParser
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideDB(app: Application, moshi : Moshi) : ApplicationDB {
        return Room.databaseBuilder(app, ApplicationDB::class.java, "relic.db").build().apply {
            // set the moshi adapter here bc we cannot inject moshi into type converter class
            TypeConverters.moshi = moshi
        }
    }

    @Singleton
    @Provides
    fun provideMoshi() : Moshi {
        return Deserializer.getInstance()
    }

    @Singleton
    @Provides
    fun provideJSONParser() : JSONParser{
        return JSONParser()
    }
}