package com.relic.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import com.relic.data.entities.CommentEntity;

import java.util.List;

@Dao
public abstract class CommentDao {
  @Insert
  abstract void insertComments(List<CommentEntity> commentEntities);
}
