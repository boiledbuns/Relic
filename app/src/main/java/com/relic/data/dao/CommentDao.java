package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.domain.models.CommentModel;

import java.util.List;

@Dao
public abstract class CommentDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertComments(List<CommentModel> commentEntities);

    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN PostSourceEntity ON CommentModel.id = PostSourceEntity.sourceId " +
        "WHERE linkFullname = :postFullname ORDER BY position")
    public abstract LiveData<List<CommentModel>> getAllComments(String postFullname);

    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN PostSourceEntity ON CommentModel.id = PostSourceEntity.sourceId " +
        "WHERE linkFullname = :postFullname AND depth < :depth ORDER BY depth ASC")
    public abstract LiveData<List<CommentModel>> getChildrenByLevel(String postFullname, int depth);

    @Query("DELETE from CommentModel WHERE linkFullname = :postFullname")
    public abstract void deletePostComments(String postFullname);

    @Query("DELETE from CommentModel WHERE id = :commentFullName")
    public abstract void deleteComment(String commentFullName);
}
