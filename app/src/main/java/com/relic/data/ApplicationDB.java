package com.relic.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.relic.data.dao.AccountDao;
import com.relic.data.dao.CommentDao;
import com.relic.data.dao.ListingDao;
import com.relic.data.dao.PostDao;
import com.relic.data.dao.PostSourceRelationDao;
import com.relic.data.dao.PostVisitedRelationDao;
import com.relic.data.dao.SubredditDao;
import com.relic.data.dao.TokenStoreDao;
import com.relic.data.dao.UserPostingDao;
import com.relic.data.entities.AccountEntity;
import com.relic.data.entities.ListingEntity;
import com.relic.data.entities.PostVisitRelation;
import com.relic.data.entities.SourceAndPostRelation;
import com.relic.data.entities.SubredditEntity;
import com.relic.data.entities.TokenStoreEntity;
import com.relic.domain.models.CommentModel;
import com.relic.domain.models.PostModel;

@Database(
    entities = {
        SubredditEntity.class,
        PostModel.class,
        SourceAndPostRelation.class,
        ListingEntity.class,
        CommentModel.class,
        AccountEntity.class,
        TokenStoreEntity.class,
        PostVisitRelation.class
    },
    version = 7,
    exportSchema = false
)
@TypeConverters(com.relic.data.TypeConverters.class)
public abstract class ApplicationDB extends RoomDatabase{
  private static ApplicationDB INSTANCE;

  public abstract SubredditDao getSubredditDao();
  public abstract PostDao getPostDao();
  public abstract PostSourceRelationDao getPostSourceDao();
  public abstract ListingDao getListingDAO();
  public abstract CommentDao getCommentDAO();
  public abstract UserPostingDao getUserPostingDao();
  public abstract AccountDao getAccountDao();
  public abstract TokenStoreDao getTokenStoreDao();
  public abstract PostVisitedRelationDao getPostVisitedDao();

    public static ApplicationDB getDatabase(Context context) {
    if(INSTANCE == null) {
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
          ApplicationDB.class, "APP").build();
    }
    return INSTANCE;
  }

}
