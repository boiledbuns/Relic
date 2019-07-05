package com.relic.persistence;

import android.app.Application;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.relic.domain.models.SubredditModel;
import com.relic.persistence.dao.AccountDao;
import com.relic.data.dao.relation.PostSourceRelationDao;
import com.relic.data.dao.relation.PostVisitedRelationDao;
import com.relic.persistence.dao.CommentDao;
import com.relic.persistence.dao.ListingDao;
import com.relic.persistence.dao.PostDao;
import com.relic.persistence.dao.SubredditDao;
import com.relic.persistence.dao.TokenStoreDao;
import com.relic.persistence.dao.UserPostingDao;
import com.relic.persistence.entities.AccountEntity;
import com.relic.persistence.entities.ListingEntity;
import com.relic.persistence.entities.PostVisitRelation;
import com.relic.persistence.entities.SourceAndPostRelation;
import com.relic.persistence.entities.TokenStoreEntity;
import com.relic.domain.models.CommentModel;
import com.relic.domain.models.PostModel;

@Database(
    entities = {
        SubredditModel.class,
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
@TypeConverters(com.relic.persistence.TypeConverters.class)
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

    public static ApplicationDB getDatabase(Application app) {
        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                app,
                ApplicationDB.class,
                "RELIC"
            ).build();
        }
        return INSTANCE;
    }
}
