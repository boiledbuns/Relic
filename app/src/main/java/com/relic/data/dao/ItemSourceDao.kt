package com.relic.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.relic.data.models.ItemSourceModel

@Dao
abstract class ItemSourceDao {
    @Query("SELECT * FROM PostSourceEntity INNER JOIN PostEntity ON sourceId=PostEntity.id " +
        "INNER JOIN CommentEntity ON sourceId=CommentEntity.id")
    abstract fun getItems() : LiveData<List<ItemSourceModel>>
}