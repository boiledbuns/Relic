package com.relic.data.dao

import android.arch.persistence.room.*
import com.relic.data.entities.PostSourceEntity
import java.util.ArrayList

@Dao
abstract class PostSourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPostSources(postSourceEntities: List<PostSourceEntity>)

    @Query("SELECT * FROM PostSourceEntity WHERE sourceId = :postId")
    abstract fun getPostSource(postId: String): PostSourceEntity?

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE subredditPosition >= 0 AND subreddit = :subredditName")
    abstract fun getItemsCountForSubreddit(subredditName: String): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE frontpagePosition >= 0")
    abstract fun getItemsCountForFrontpage(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE allPosition >= 0")
    abstract fun getItemsCountForAll(): Int

    // region user posts

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE userSubmittedPosition >= 0")
    abstract fun getItemsCountForUserSubmitted(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE userCommentsPosition >= 0")
    abstract fun getItemsCountForUserComments(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE userUpvotedPosition >= 0")
    abstract fun getItemsCountForUserUpvoted(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE userDownvotedPosition >= 0")
    abstract fun getItemsCountForUserDownvoted(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE userSavedPosition >= 0")
    abstract fun getItemsCountForUserSaved(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE userGildedPosition >= 0")
    abstract fun getItemsCountForUserGilded(): Int

    @Query("SELECT COUNT() FROM PostSourceEntity WHERE userHiddenPosition >= 0")
    abstract fun getItemsCountForUserHidden(): Int

    // region remove source from all posts based on criteria

    @Query("UPDATE PostSourceEntity SET subredditPosition = -1 WHERE subreddit = :subName")
    abstract fun removeAllSubredditAsSource(subName: String)

    @Query("UPDATE PostSourceEntity SET frontpagePosition = -1 WHERE frontpagePosition >= 0")
    abstract fun removeAllFrontpageAsSource()

    @Query("UPDATE PostSourceEntity SET allPosition = -1 WHERE allPosition >= 0")
    abstract fun removeAllAllAsSource()

    // endregion user posts

    // I honestly hate doing this, but I haven't yet found a good solution
    // that saves me the pain of writing n queries using room yet

    @Query("UPDATE PostSourceEntity SET userSubmittedPosition = -1 WHERE userSubmittedPosition >= 0")
    abstract fun removeAllUserSubmittedAsSource()

    @Query("UPDATE PostSourceEntity SET userCommentsPosition = -1 WHERE userCommentsPosition >= 0")
    abstract fun removeAllUserCommentsAsSource()

    @Query("UPDATE PostSourceEntity SET userSavedPosition = -1 WHERE userSavedPosition >= 0")
    abstract fun removeAllUserSavedAsSource()

    @Query("UPDATE PostSourceEntity SET userUpvotedPosition = -1 WHERE userUpvotedPosition >= 0")
    abstract fun removeAllUserUpvotedAsSource()

    @Query("UPDATE PostSourceEntity SET userDownvotedPosition = -1 WHERE userDownvotedPosition >= 0")
    abstract fun removeAllUserDownvotedAsSource()

    @Query("UPDATE PostSourceEntity SET userGildedPosition = -1 WHERE userGildedPosition >= 0")
    abstract fun removeAllUserGildedAsSource()

    @Query("UPDATE PostSourceEntity SET userHiddenPosition = -1 WHERE userHiddenPosition >= 0")
    abstract fun removeAllUserHiddenAsSource()
    // endregion

    // region remove source from single post based on criteria
    @Query("UPDATE PostSourceEntity SET subredditPosition = -1 WHERE sourceId = :postId")
    abstract fun removeSubredditAsSource(postId: String)

    @Query("UPDATE PostSourceEntity SET frontpagePosition = -1 WHERE sourceId = :postId")
    abstract fun removeFrontpageAsSource(postId: String)

    @Query("UPDATE PostSourceEntity SET allPosition = -1 WHERE sourceId = :postId")
    abstract fun removeAllAsSource(postId: String)
    // endregion

    @Query("DELETE FROM PostSourceEntity " +
        "WHERE subredditPosition < 0 AND frontpagePosition < 0 AND allPosition < 0 " +
        "AND userSubmittedPosition < 0 AND userCommentsPosition < 0 AND userSavedPosition < 0 " +
        "AND userUpvotedPosition < 0 AND userDownvotedPosition < 0 AND userGildedPosition < 0 " +
        "AND userHiddenPosition < 0")
    abstract fun removeAllUnusedSources()
}