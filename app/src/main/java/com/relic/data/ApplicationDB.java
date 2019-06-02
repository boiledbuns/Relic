package com.relic.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.relic.data.dao.AccountDao;
import com.relic.data.dao.CommentDao;
import com.relic.data.dao.ListingDao;
import com.relic.data.dao.PostDao;
import com.relic.data.dao.PostSourceDao;
import com.relic.data.dao.SubredditDao;
import com.relic.data.dao.TokenStoreDao;
import com.relic.data.dao.UserPostingDao;
import com.relic.data.entities.AccountEntity;
import com.relic.data.entities.CommentEntity;
import com.relic.data.entities.ListingEntity;
import com.relic.data.entities.PostEntity;
import com.relic.data.entities.PostSourceEntity;
import com.relic.data.entities.SubredditEntity;
import com.relic.data.entities.TokenStoreEntity;

@Database(
    entities = {
        SubredditEntity.class,
        PostEntity.class,
        PostSourceEntity.class,
        ListingEntity.class,
        CommentEntity.class,
        AccountEntity.class,
        TokenStoreEntity.class
    },
    version = 7,
    exportSchema = false
)
public abstract class ApplicationDB extends RoomDatabase{
  private static ApplicationDB INSTANCE;

  public abstract SubredditDao getSubredditDao();
  public abstract PostDao getPostDao();
  public abstract PostSourceDao getPostSourceDao();
  public abstract ListingDao getListingDAO();
  public abstract CommentDao getCommentDAO();
  public abstract UserPostingDao getUserPostingDao();
  public abstract AccountDao getAccountDao();
  public abstract TokenStoreDao getTokenStoreDao();

    public static ApplicationDB getDatabase(Context context) {
    if(INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
          ApplicationDB.class, "APP").build();
    }
    return INSTANCE;
  }

}
