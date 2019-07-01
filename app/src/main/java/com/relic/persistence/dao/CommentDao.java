package com.relic.persistence.dao;

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

    @Query("SELECT * FROM CommentModel WHERE linkFullname = :postFullname")
    public abstract List<CommentModel> getAllComments(String postFullname);

    @Query("SELECT * FROM CommentModel " +
        "LEFT JOIN SourceAndPostRelation ON id = postId " +
        "WHERE linkFullname = :postFullname AND depth < :depth ORDER BY depth ASC")
    public abstract List<CommentModel> getChildrenByLevel(String postFullname, int depth);

    @Query("DELETE from CommentModel WHERE linkFullname = :postFullname")
    public abstract void deletePostComments(String postFullname);

    @Query("DELETE from CommentModel WHERE id = :commentFullName")
    public abstract void deleteComment(String commentFullName);
}
