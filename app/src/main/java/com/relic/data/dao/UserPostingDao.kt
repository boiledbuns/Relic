package com.relic.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel

@Dao
abstract class UserPostingDao {
// for user specific actions

    @Query("SELECT * FROM PostModel INNER JOIN SourceAndPostRelation ON id = postId WHERE source = :sourceName ORDER BY position ASC")
    abstract fun getUserPosts(sourceName : String): LiveData<List<PostModel>>

    @Query("SELECT * FROM CommentModel INNER JOIN SourceAndPostRelation ON id = postId WHERE source = :sourceName ORDER BY position ASC")
    abstract fun getUserComments(sourceName : String): LiveData<List<CommentModel>>
}