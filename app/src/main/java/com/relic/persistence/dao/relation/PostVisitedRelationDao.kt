package com.relic.data.dao.relation

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.relic.persistence.entities.PostVisitRelation

@Dao
abstract class PostVisitedRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertVisited(postVisit: PostVisitRelation)

    @Query("SELECT visitedFullname FROM PostVisitRelation WHERE visitedFullname IN (:postFullnames)")
    abstract fun getVisited(postFullnames : List<String>) : List<String>
}