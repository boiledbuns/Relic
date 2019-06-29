package com.relic.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.relic.domain.models.PostModel;

import java.util.List;

@Dao
public abstract class PostDao {
    // TODO : Currently can't use constants from entity class in query annotation so hardcoded for now
    // region get posts based on origin

    @Query("SELECT * FROM PostModel INNER JOIN SourceAndPostRelation ON id = postId WHERE source = :sourceName ORDER BY position ASC")
    public abstract LiveData<List<PostModel>> getPostsFromSource(String sourceName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPosts(List<PostModel> posts);

    @Query("SELECT * FROM PostModel INNER JOIN SourceAndPostRelation ON id = postId WHERE fullName = :fullName")
    public abstract LiveData<PostModel> getSinglePost(String fullName);

    @Query("SELECT * FROM PostModel WHERE subreddit = :subreddit AND fullName = '' AND author = ''")
    public abstract PostModel getPostDraft(String subreddit);

    @Query("DELETE FROM PostModel WHERE subreddit = :subreddit AND fullName = '' AND author = ''")
    public abstract void deletePostDraft(String subreddit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPost(PostModel post);

    @Query("UPDATE PostModel SET visited = 1 where fullName = :postFullname")
    public abstract void updateVisited(String postFullname);

    @Query("UPDATE PostModel SET userUpvoted = :vote  where fullName = :postFullname")
    public abstract void updateVote(String postFullname, int vote);

    @Query("UPDATE PostModel SET saved = :saved  where fullName = :postFullname")
    public abstract void updateSave(String postFullname, boolean saved);

    @Query("SELECT * FROM PostModel WHERE fullName = :postFullname LIMIT 1")
    public abstract PostModel getPostWithId(String postFullname);

    @Query("DELETE FROM PostModel WHERE id IN ((SELECT id FROM SourceAndPostRelation) NOT IN (SELECT id FROM PostModel))")
    public abstract void deletePostsWithoutSources();
}
