package com.relic.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.relic.data.entities.PostVisitRelation

@Dao
abstract class PostVisitedRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertVisited(postVisit: PostVisitRelation)
}