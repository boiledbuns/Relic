package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
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

  @Query("SELECT id, author, body, created, score, isSubmitter, gilded FROM CommentEntity WHERE parentId = :postId")
  public abstract LiveData<List<CommentModel>> getComments(String postId);

  @Query("DELETE from CommentEntity WHERE parentId = :postFullname")
  public abstract void deletePostComments(String postFullname);
}
