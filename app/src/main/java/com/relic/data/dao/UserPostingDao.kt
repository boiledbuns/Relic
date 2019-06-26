package com.relic.data.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel

@Dao
abstract class UserPostingDao {
// for user specific actions

    @Query("SELECT * FROM PostModel " +
        "LEFT JOIN PostSourceEntity ON PostModel.fullName = PostSourceEntity.sourceId " +
        "WHERE PostSourceEntity.userSubmittedPosition >= 0 ORDER BY PostSourceEntity.userSubmittedPosition ASC")
    abstract fun getUserPosts(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostModel " +
        "LEFT JOIN PostSourceEntity ON PostModel.fullName = PostSourceEntity.sourceId " +
        "WHERE PostSourceEntity.userSavedPosition >= 0 ORDER BY PostSourceEntity.userSavedPosition ASC")
    abstract fun getUserSavedPosts(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostModel " +
        "LEFT JOIN PostSourceEntity ON PostModel.fullName = PostSourceEntity.sourceId " +
        "WHERE PostSourceEntity.userUpvotedPosition >= 0 ORDER BY PostSourceEntity.userUpvotedPosition ASC")
    abstract fun getUserUpvotedPosts(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostModel " +
        "LEFT JOIN PostSourceEntity ON PostModel.fullName = PostSourceEntity.sourceId " +
        "WHERE  PostSourceEntity.userDownvotedPosition >= 0 ORDER BY PostSourceEntity.userDownvotedPosition ASC")
    abstract fun getUserDownvotedPosts(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostModel " +
        "LEFT JOIN PostSourceEntity ON PostModel.fullName = PostSourceEntity.sourceId " +
        "WHERE PostSourceEntity.userGildedPosition >= 0 ORDER BY PostSourceEntity.userGildedPosition ASC")
    abstract fun getUserGilded(): LiveData<List<PostModel>>

    @Query("SELECT * FROM PostModel " +
        "LEFT JOIN PostSourceEntity ON PostModel.fullName = PostSourceEntity.sourceId " +
        "WHERE PostSourceEntity.userHiddenPosition >= 0 ORDER BY PostSourceEntity.userHiddenPosition ASC")
    abstract fun getUserHidden(): LiveData<List<PostModel>>


    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN PostSourceEntity ON CommentModel.id = PostSourceEntity.sourceId " +
        "WHERE CommentModel.userCommentsPosition >= 0 ORDER BY CommentModel.userCommentsPosition ASC")
    abstract fun getUserComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN PostSourceEntity ON CommentModel.id = PostSourceEntity.sourceId " +
        "WHERE CommentModel.userSavedPosition >= 0 ORDER BY CommentModel.userSavedPosition ASC")
    abstract fun getUserSavedComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN PostSourceEntity ON CommentModel.id = PostSourceEntity.sourceId " +
        "WHERE CommentModel.userUpvotedPosition >= 0 ORDER BY CommentModel.userUpvotedPosition ASC")
    abstract fun getUserUpvotedComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN PostSourceEntity ON CommentModel.id = PostSourceEntity.sourceId " +
        "WHERE CommentModel.userDownvotedPosition >= 0 ORDER BY CommentModel.userDownvotedPosition ASC")
    abstract fun getUserDownvotedComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN PostSourceEntity ON CommentModel.id = PostSourceEntity.sourceId " +
        "WHERE CommentModel.userGildedPosition >= 0 ORDER BY CommentModel.userGildedPosition ASC")
    abstract fun getUserGildedComments(): LiveData<List<CommentModel>>

    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN PostSourceEntity ON CommentModel.id = PostSourceEntity.sourceId " +
        "WHERE CommentModel.userHiddenPosition >= 0 ORDER BY CommentModel.userHiddenPosition ASC")
    abstract fun getUserHiddenComments(): LiveData<List<CommentModel>>
}