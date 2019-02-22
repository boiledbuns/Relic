package com.relic.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.relic.data.models.CommentModel
import com.relic.data.models.PostModel

@Dao
abstract class UserPostingDao {
// for user specific actions

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userSubmittedPosition >= 0 ORDER BY userSubmittedPosition ASC")
    abstract fun getUserPosts(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userSavedPosition >= 0 ORDER BY userSavedPosition ASC")
    abstract fun getUserSavedPosts(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userUpvotedPosition >= 0 ORDER BY userUpvotedPosition ASC")
    abstract fun getUserUpvotedPosts(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE  userDownvotedPosition >= 0 ORDER BY userDownvotedPosition ASC")
    abstract fun getUserDownvotedPosts(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userGildedPosition >= 0 ORDER BY userGildedPosition ASC")
    abstract fun getUserGilded(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userHiddenPosition >= 0 ORDER BY userHiddenPosition ASC")
    abstract fun getUserHidden(): LiveData<List<PostModel>>


    @Query("SELECT * FROM CommentEntity " +
        "LEFT JOIN PostSourceEntity ON CommentEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userCommentsPosition >= 0 ORDER BY userCommentsPosition ASC")
    abstract fun getUserComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentEntity " +
        "LEFT JOIN PostSourceEntity ON CommentEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userSavedPosition >= 0 ORDER BY userSavedPosition ASC")
    abstract fun getUserSavedComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentEntity " +
        "LEFT JOIN PostSourceEntity ON CommentEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userUpvotedPosition >= 0 ORDER BY userUpvotedPosition ASC")
    abstract fun getUserUpvotedComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentEntity " +
        "LEFT JOIN PostSourceEntity ON CommentEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE  userDownvotedPosition >= 0 ORDER BY userDownvotedPosition ASC")
    abstract fun getUserDownvotedComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentEntity " +
        "LEFT JOIN PostSourceEntity ON CommentEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userGildedPosition >= 0 ORDER BY userGildedPosition ASC")
    abstract fun getUserGildedComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentEntity " +
        "LEFT JOIN PostSourceEntity ON CommentEntity.fullName = PostSourceEntity.sourceId " +
        "WHERE userHiddenPosition >= 0 ORDER BY userHiddenPosition ASC")
    abstract fun getUserHiddenComments(): LiveData<List<CommentModel>>
}