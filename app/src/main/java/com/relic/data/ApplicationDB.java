package com.relic.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.relic.data.dao.ListingDAO;
import com.relic.data.dao.PostDao;
import com.relic.data.dao.SubredditDao;
import com.relic.data.entities.ListingEntity;
import com.relic.data.entities.PostEntity;
import com.relic.data.models.SubredditModel;

@Database(entities = {SubredditModel.class, PostEntity.class, ListingEntity.class}, version = 4, exportSchema = false)
public abstract class ApplicationDB extends RoomDatabase{
  private static ApplicationDB INSTANCE;

  public abstract SubredditDao getSubredditDao();
  public abstract PostDao getPostDao();
  public abstract ListingDAO getListingDAO();


  public static ApplicationDB getDatabase(Context context) {
    if(INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
          ApplicationDB.class, "APP").build();
    }
    return INSTANCE;
  }

}
