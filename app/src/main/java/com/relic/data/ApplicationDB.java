package com.relic.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.relic.data.dao.SubredditDao;
import com.relic.data.models.SubredditModel;

@Database(entities = {SubredditModel.class}, version = 1, exportSchema = false)
public abstract class ApplicationDB extends RoomDatabase{
  private static ApplicationDB INSTANCE;

  public abstract SubredditDao getSubredditDao();

  public static ApplicationDB getDatabase(Context context) {
    if(INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
          ApplicationDB.class, "SUBREDDIT").build();
    }

    return INSTANCE;
  }

}
