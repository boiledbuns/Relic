package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.data.entities.PostEntity;
import com.relic.data.models.PostModel;

import java.util.List;

@Dao
public abstract class PostDao {
    // TODO : Currently can't use constants from entity class in query annotation so hardcoded for now

    // region get posts based on origin

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.id = PostSourceEntity.sourceId " +
        "WHERE subredditPosition >= 0 AND PostSourceEntity.subreddit = :subredditName ORDER BY subredditPosition ASC")
    public abstract LiveData<List<PostModel>> getPostsFromSubreddit(String subredditName);

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.id = PostSourceEntity.sourceId " +
        "WHERE frontpagePosition >= 0 ORDER BY frontpagePosition ASC")
    public abstract LiveData<List<PostModel>> getPostsFromFrontpage();

    @Query("SELECT * FROM PostEntity " +
        "LEFT JOIN PostSourceEntity ON PostEntity.id = PostSourceEntity.sourceId " +
        "WHERE allPosition >= 0 ORDER BY allPosition ASC")
    public abstract LiveData<List<PostModel>> getPostsFromAll();

//    @Query("SELECT * FROM PostEntity WHERE subredditPosition >= 0 AND subreddit = :subredditName ORDER BY `subredditPosition` ASC")
//    public abstract LiveData<List<PostModel>> getPostsFromSubreddit1(String subredditName);
//
//    @Query("SELECT * FROM PostEntity WHERE frontpagePosition >= 0 ORDER BY `frontpagePosition` ASC")
//    public abstract LiveData<List<PostModel>> getPostsFromFrontpage();
//
//    @Query("SELECT * FROM PostEntity WHERE allPosition >= 0 ORDER BY `allPosition` ASC")
//    public abstract LiveData<List<PostModel>> getPostsFromAll();

    // endregion get posts based on origin

//    @Query("UPDATE PostEntity SET subredditPosition = -1 WHERE subreddit = :subName")
//    public abstract void removeSubredditAsOrigin(String subName);
//
//    @Query("UPDATE PostEntity SET frontpagePosition = -1 WHERE subreddit = :subName")
//    public abstract void removeFrontpageAsOrigin(String subName);
//
//    @Query("UPDATE PostEntity SET allPosition = -1 WHERE subreddit = :subName")
//    public abstract void removeAllAsOrigin(String subName);

    // endregion remove origin from post

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPosts(List<PostEntity> posts);

//    @Query("DELETE FROM PostEntity WHERE subreddit = :subName")
//    public abstract void deleteAllFromSub(String subName);

    @Query("DELETE FROM PostEntity WHERE PostEntity.id IN " +
        "(SELECT sourceId FROM PostSourceEntity WHERE PostSourceEntity.sourceId >= 0 AND subreddit = :subName)")
    public abstract void deleteAllFromSub(String subName);

    @Query("DELETE FROM PostEntity WHERE PostEntity.id IN " +
        "(SELECT sourceId FROM PostSourceEntity WHERE PostSourceEntity.sourceId >= 0)")
    public abstract void deleteAllFromFrontpage();

    @Query("DELETE FROM PostEntity WHERE PostEntity.id IN " +
        "(SELECT sourceId FROM PostSourceEntity WHERE PostSourceEntity.sourceId >= 0)")
    public abstract void deleteAllFromAll();

    @Query("SELECT * FROM PostEntity INNER JOIN PostSourceEntity ON id = sourceId WHERE id = :postName")
    public abstract LiveData<PostModel> getSinglePost(String postName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPost(PostEntity post);

    @Query("UPDATE PostEntity SET visited = 1 where id = :postFullname")
    public abstract void updateVisited(String postFullname);

    @Query("UPDATE PostEntity SET userUpvoted = :vote  where id = :postFullname")
    public abstract void updateVote(String postFullname, int vote);

    @Query("UPDATE PostEntity SET saved = :saved  where id = :postFullname")
    public abstract void updateSave(String postFullname, boolean saved);

    @Query("SELECT * FROM PostEntity WHERE id = :postFullname LIMIT 1")
    public abstract PostEntity getPostWithId(String postFullname);
}
