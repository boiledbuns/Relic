package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.CommentEntity;
import com.relic.data.models.CommentModel;

import java.util.List;

@Dao
public abstract class CommentDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertComments(List<CommentEntity> commentEntities);

    @Query("SELECT * FROM CommentEntity " +
        "LEFT JOIN PostSourceEntity ON CommentEntity.id = PostSourceEntity.sourceId " +
        "WHERE parentPostId = :postId ORDER BY position")
    public abstract LiveData<List<CommentModel>> getAllComments(String postId);

    @Query("SELECT * FROM CommentEntity " +
        "LEFT JOIN PostSourceEntity ON CommentEntity.id = PostSourceEntity.sourceId " +
        "WHERE parentId = :parentId AND depth < :depth ORDER BY depth ASC")
    public abstract LiveData<List<CommentModel>> getChildrenByLevel(String parentId, int depth);

    @Query("DELETE from CommentEntity WHERE parentPostId = :postId")
    public abstract void deletePostComments(String postId);
}
