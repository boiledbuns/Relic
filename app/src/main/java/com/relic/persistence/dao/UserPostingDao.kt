package com.relic.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
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