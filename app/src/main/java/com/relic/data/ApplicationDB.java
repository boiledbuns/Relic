package com.relic.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.relic.data.dao.CommentDao;
import com.relic.data.dao.ListingDao;
import com.relic.data.dao.PostDao;
import com.relic.data.dao.SubredditDao;
import com.relic.data.entities.CommentEntity;
import com.relic.data.entities.ListingEntity;
import com.relic.data.entities.PostEntity;
import com.relic.data.entities.SubredditEntity;

@Database(entities = {SubredditEntity.class, PostEntity.class, ListingEntity.class, CommentEntity.class}, version = 7, exportSchema = false)
public abstract class ApplicationDB extends RoomDatabase{
  private static ApplicationDB INSTANCE;

  public abstract SubredditDao getSubredditDao();
  public abstract PostDao getPostDao();
  public abstract ListingDao getListingDAO();
  public abstract CommentDao getCommentDAO();


  public static ApplicationDB getDatabase(Context context) {
    if(INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
          ApplicationDB.class, "APP").build();
    }
    return INSTANCE;
  }

}
