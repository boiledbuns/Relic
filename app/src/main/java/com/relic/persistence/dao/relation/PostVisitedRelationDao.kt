package com.relic.data.dao.relation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.relic.persistence.entities.PostVisitRelation

@Dao
abstract class PostVisitedRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertVisited(postVisit: PostVisitRelation)

    @Query("SELECT visitedFullname FROM PostVisitRelation WHERE visitedFullname IN (:postFullnames)")
    abstract fun getVisited(postFullnames : List<String>) : List<String>
}