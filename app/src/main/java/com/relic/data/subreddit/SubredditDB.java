package com.relic.data.subreddit;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {SubredditDecorator.class}, version = 1, exportSchema = false)
public abstract class SubredditDB extends RoomDatabase{
  private static SubredditDB INSTANCE;

  public abstract SubredditDao getSubredditDao();

  public static SubredditDB getDatabase(Context context) {
    if(INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
          SubredditDB.class, "SUBREDDIT").build();
    }

    return INSTANCE;
  }

}
