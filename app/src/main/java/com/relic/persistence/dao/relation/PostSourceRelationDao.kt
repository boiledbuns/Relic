package com.relic.data.dao.relation

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.relic.persistence.entities.SourceAndPostRelation

@Dao
abstract class PostSourceRelationDao {
    @Insert
    abstract fun insertPostSourceRelations(relationEntities: List<SourceAndPostRelation>)

    @Query("SELECT COUNT() FROM SourceAndPostRelation WHERE source = :sourceName")
    abstract fun getItemsCountForSource(sourceName : String) : Int

    @Query("DELETE FROM SourceAndPostRelation WHERE source = :sourceName")
    abstract fun removeAllFromSource(sourceName: String)
}